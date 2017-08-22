package de.unituebingen.sfs.brillmoore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ryangantt.util.Trie;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;
import de.unituebingen.sfs.brillmoore.aligner.AlignmentUtils;
import de.unituebingen.sfs.brillmoore.aligner.LevenshteinAligner;
import de.unituebingen.sfs.brillmoore.errormodel.ErrorModel;

public class SpellChecker 
{
	private Trie<Trie<Double>> betaAlphaTrie;
	private Map<String, Double> dict;
	private int window;
	private double minAtoA;
	private final int paddingLength = 2;
	private String reservedChars = AlignmentUtils.getReservedChars();
	private String regexReservedChars = ".*[" + reservedChars + "].*";
	private String reservedCharsErrorMessage = "Please edit the data or modify AlignmentUtils to choose unused characters.";

	public SpellChecker(List<Misspelling> misspellings, Map<String, Double> aDict, int window, double minAtoA) throws ParseException {
		this.dict = aDict;
		this.window = window;
		this.minAtoA = minAtoA;
		
		// check for reserved characters in dictionary and misspellings
		for (String dictKey : aDict.keySet()) {
			if (dictKey.matches(regexReservedChars)) {
				throw new ParseException("The dictionary contains the reserved characters: " + 
						reservedChars + "\n" + reservedCharsErrorMessage, 0);
			}
		}
		
		for (Misspelling m : misspellings) {
			if (m.getSource().matches(regexReservedChars) || 
					m.getTarget().matches(regexReservedChars)) {
				throw new ParseException("The training data contains the reserved characters: " + 
						reservedChars + "\n" + reservedCharsErrorMessage, 0);
		
			}
		}
		
		trainSpellChecker(misspellings);
	}
	
	private void trainSpellChecker(List<Misspelling> misspellings) throws ParseException {
		Map<Alignment, Integer> alignmentCounts = new HashMap<>();

		LevenshteinAligner la = new LevenshteinAligner(1, 1, 1);
		
		for (Misspelling m : misspellings) {
			String source = m.getSource();
			String target = m.getTarget();
			int count = m.getCount();
			List<Alignment> alignments = la.getAlignments(target, source);
			List<Alignment> expandedAlignments = AlignmentUtils.extendAlignments(alignments, window);
			
			for (Alignment a : expandedAlignments) {
				Integer prevCount = alignmentCounts.get(a);

				if (prevCount == null) {
					prevCount = 0;
				}
				
				alignmentCounts.put(a, prevCount + count);
			}
		}

		// generate an error model from the alignment counts, with a default
		// minimum probability for alpha -> alpha (m from Boyd (2008), p. 24)
		ErrorModel e = new ErrorModel(alignmentCounts, minAtoA);

		// create beta/alpha trie
		makeBetaAlphaTrie(e);
	}
	
	public List<Candidate> getRankedCandidates(final String m, Map<String, Double> aDict) throws ParseException {
		// traverse the dictionary trie to calculate the edit distance between 
		// a misspelling and all words in the dictionary
		
		// check for reserved characters in the misspelling
		if (m.matches(regexReservedChars)) {
			throw new ParseException("The misspelling / test data contains the reserved characters: " + 
					reservedChars + "\n" + reservedCharsErrorMessage, 0);
		}
		
		// check for reserved characters in custom dictionary
		for (String dictKey : aDict.keySet()) {
			if (dictKey.matches(regexReservedChars)) {
				throw new ParseException("The dictionary contains the reserved characters: " + 
						reservedChars + "\n" + reservedCharsErrorMessage, 0);
			}
		}
	
		String misspelling = AlignmentUtils.padWord(m);
		
		return editDist(misspelling, aDict);
	}
	
	public List<Candidate> getRankedCandidates(final String m) throws ParseException {		
		return getRankedCandidates(m, dict);
	}
	
	private List<Candidate> editDist(final String m, Map<String, Double> aDict) {
		// create a new dictionary trie for each calculation
		Trie<List<Double>> dictTrie = makeDictTrie(aDict);
		
		// initialize values in dictTrie for new calculation		
		dictTrie.clearValues();
		dictTrie.getRoot().setValue(new ArrayList<Double>());
		while (dictTrie.getRoot().getValue().size() < m.length()) {
			dictTrie.getRoot().getValue().add(Double.POSITIVE_INFINITY);
		}
		
		editDistCalc(m, "", dictTrie.getRoot());
		List<Candidate> candidates = getRankedCandidates(dictTrie, window, aDict);
		
		return candidates;
	}
	
	private void editDistCalc(final String m, final String prefix, final Trie<List<Double>>.Node node) {

		// at root initialize first row of edit distance table
		if (node.getParent() == null) {
			node.getValue().set(0, 0.0);

			for (int i = 0; i < m.length(); i++) {
				String sstr = m.substring(0, i);
				node.getValue().set(i, getProb(new Alignment(AlignmentUtils.nullString, sstr)));
			}
		}

		// initialize value of this node if necessary
		if (node.getValue() == null) {
			node.setValue(new ArrayList<Double>());
			while (node.getValue().size() < m.length()) {
				node.getValue().add(Double.POSITIVE_INFINITY);
			}
		}

		node.getValue().set(0, getProb(new Alignment(prefix, AlignmentUtils.nullString)));

		for (int i = 1; i < m.length(); i++) {
			String sstr = prefix;
			String tstr = m.substring(0, i);

			double lowest = Double.POSITIVE_INFINITY;
			double e, e1, e2;

			// get the beta trie node corresponding to the last character
			// in the target string
			Trie<Trie<Double>>.Node betaTrieNode = betaAlphaTrie
					.getNode(tstr.length() == 0 ? AlignmentUtils.nullString : tstr.substring(tstr.length() - 1));

			for (int j = tstr.length(); j >= 0 && j >= tstr.length() - window - 1; j--) {
				Trie<List<Double>>.Node pnode = node;

				Trie<Trie<Double>>.Node relevantBetaTrieNode = betaTrieNode;

				// in the first iteration the target (beta) substring
				// is empty, so replace the beta trie node with the one
				// for the empty string
				if (betaAlphaTrie != null && j == tstr.length()) {
					relevantBetaTrieNode = betaAlphaTrie.getNode(AlignmentUtils.nullString);
				}

				// get the alpha trie node corresponding to the last 
				// character in the source string
				Trie<Double> alphaTrie = null;
				Trie<Double>.Node alphaTrieNode = null;

				if (relevantBetaTrieNode != null) {
					alphaTrie = relevantBetaTrieNode.getValue();
					if (alphaTrie != null) {
						alphaTrieNode = alphaTrie.getNode(
								sstr.length() == 0 ? AlignmentUtils.nullString : sstr.substring(sstr.length() - 1));
					}
				}

				for (int k = sstr.length(); k >= 0 && k >= sstr.length() - window - 1; k--) {
					Trie<Double>.Node relevantAlphaTrieNode = alphaTrieNode;

					// in the first iteration the source (alpha) substring 
					// is empty, so replace the alpha trie node with the one 
					// for the empty string
					if (alphaTrie != null && k == sstr.length()) {
						relevantAlphaTrieNode = alphaTrie.getNode(AlignmentUtils.nullString);
					}

					if (pnode == null) {
						e1 = node.getValue().get(j);
					} else {
						e1 = pnode.getValue().get(j);
						pnode = pnode.getParent();
					}

					e2 = getProb(relevantAlphaTrieNode);

					e = e1 + e2;

					lowest = Math.min(e, lowest);

					// for the first iteration, alphaTrieNode is replaced with
					// the alpha trie node for the null string, so only move down 
					// the alpha trie starting at the second iteration
					if (k < sstr.length()) {
						if (alphaTrieNode != null && k > 0) {
							alphaTrieNode = alphaTrieNode.findChild(String.valueOf(sstr.charAt(k - 1)));
						} else {
							alphaTrieNode = null;
						}
					}
				}

				// for the first iteration, betaTrieNode is replaced with
				// the beta trie node for the null string, so only move down 
				// the beta trie starting at the second iteration
				if (j < tstr.length()) {
					if (betaTrieNode != null && j > 0) {
						betaTrieNode = betaTrieNode.findChild(String.valueOf(tstr.charAt(j - 1)));
					} else {
						betaTrieNode = null;
					}
				}
			}

			node.getValue().set(i, lowest);
		}

		// traverse the children
		for (final Trie<List<Double>>.Node child : node.getChildren()) {
			if ((null != child) && (null != child.getKey())) {
				editDistCalc(m, prefix + child.getKey(), child);
			}
		}
	}

	private Trie<List<Double>> makeDictTrie(Map<String, Double> dict) {
		// create a trie for the dictionary
		Trie<List<Double>> dictTrie = new Trie<>();

		for (String w : dict.keySet()) {
			dictTrie.put(AlignmentUtils.padWord(w), new ArrayList<Double>());
		}
		
		return dictTrie;
	}

	/**
	 * Do a full lookup in the beta-alpha trie for the probability of an
	 * alignment, returning infinity if the alignment is not found.
	 * 
	 * @param a
	 * @return
	 */
	private double getProb(Alignment a) {
		if (a.lhs.length() == 0) {
			a.lhs = AlignmentUtils.nullString;
		}
		if (a.rhs.length() == 0) {
			a.rhs = AlignmentUtils.nullString;
		}

		Trie<Double> alphaTrie = betaAlphaTrie.get(StringUtils.reverse(a.rhs));

		if (alphaTrie != null) {
			Double prob = alphaTrie.get(StringUtils.reverse(a.lhs));
			if (prob != null) {
				return prob;
			}
		}

		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Find the probability at a given trie node, returning infinity if the node
	 * or node value does not exist in the trie.
	 * 
	 * @param node
	 * @return
	 */
	private Double getProb(Trie<Double>.Node node) {
		if (node != null) {
			if (node.getValue() != null) {
				return node.getValue();
			}
		}

		return Double.POSITIVE_INFINITY;
	}

	private void makeBetaAlphaTrie(ErrorModel e) {
		betaAlphaTrie = new Trie<>();

		// first create the alpha tries for each RHS
		Map<String, Trie<Double>> alphaTries = new HashMap<>();

		for (Map.Entry<Alignment, Double> a : e.getModel().entrySet()) {
			String lhs = a.getKey().lhs;
			String rhs = a.getKey().rhs;
			double prob = a.getValue();

			if (!alphaTries.containsKey(rhs)) {
				alphaTries.put(rhs, new Trie<Double>());
			}

			alphaTries.get(rhs).put(StringUtils.reverse(lhs), -Math.log(prob));

		}

		// then create the whole beta-alpha trie
		for (String rhs : alphaTries.keySet()) {
			betaAlphaTrie.put(StringUtils.reverse(rhs), alphaTries.get(rhs));
		}
	}

	/**
	 * Read the probabilities for all candidates off the dictTrie and 
	 * return a ranked list of Candidates.
	 * 
	 * @param dictTrie
	 * @param window
	 * @return
	 */
	private List<Candidate> getRankedCandidates(Trie<List<Double>> dictTrie, int window, Map<String, Double> aDict) {
		List<Candidate> c = new ArrayList<>();
		Map<String, List<Double>> v = dictTrie.traverse(true);
		
		for (Map.Entry<String, List<Double>> p : v.entrySet()) {
			String candidate = p.getKey();
			candidate = candidate.substring(paddingLength, candidate.length() - paddingLength);

			Double prob = p.getValue().get(p.getValue().size() - 1);
			prob = prob + -Math.log(aDict.get(candidate));
			
			c.add(new Candidate(candidate, prob));
		}
		
		Collections.sort(c);
		
		return c;
	}
}
