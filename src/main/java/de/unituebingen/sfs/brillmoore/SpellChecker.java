package de.unituebingen.sfs.brillmoore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ryangantt.util.Trie;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;
import de.unituebingen.sfs.brillmoore.aligner.AlignmentUtils;
import de.unituebingen.sfs.brillmoore.aligner.LevenshteinAligner;
import de.unituebingen.sfs.brillmoore.errormodel.ErrorModel;

public class SpellChecker 
{
	//private Trie<Trie<Double>> alphaBetaTrie;
	private Map<Alignment, Double> alphaBetaMap;
	private Map<String, DictEntry> dictList;
	private int window;
	private double minAtoA;

	public SpellChecker(List<Misspelling> misspellings, Map<String, DictEntry> dictList, int window, double minAtoA) {
		this.dictList = dictList;
		this.window = window;
		this.minAtoA = minAtoA;
		trainSpellChecker(misspellings);
	}
	
	private void trainSpellChecker(List<Misspelling> misspellings) {
		Map<Alignment, Integer> alignmentCounts = new HashMap<Alignment, Integer>();

		LevenshteinAligner la = new LevenshteinAligner(1, 1, 1);
		
		for (Misspelling m : misspellings) {
			String source = m.getSource();
			String target = m.getTarget();
			int count = m.getCount();
			List<Alignment> alignments = la.getAlignments(target, source);
			List<Alignment> expandedAlignments = AlignmentUtils.extendAlignments(alignments,  window);
			
			for (Alignment a : expandedAlignments) {
				Integer prevCount = alignmentCounts.get(a);

				if (prevCount == null) {
					prevCount = 0;
				}
				
				alignmentCounts.put(a, prevCount + count);
			}
		}
		
		System.out.println(alignmentCounts);
		
		// generate an error model from the alignment counts, with a default
		// minimum probabilty for alpha -> alpha (m from my thesis, p. 24)
		ErrorModel e = new ErrorModel(alignmentCounts, minAtoA);
		
		System.out.println(e);
		
		// create the alpha/beta trie with reversed strings
		//makeAlphaBetaTrie(e);
		
		// create alternate alpha/beta map
		makeAlphaBetaMap(e);

		// create dict trie (not thread-safe)
		//makeDictTrie(dict);

	}
	
	public List<Candidate> getRankedCandidates(final String m) {
		// traverse the dictionary trie to calculate the edit distance between 
		// a misspelling and all words in the dictionary
		String misspelling = padWord(m, window);
		
		return editDist(misspelling);
	}
	
	public List<Candidate> editDist(final String m) {
		// create a new dictionary trie for each calculation
		Trie<List<Double>> dictTrie = makeDictTrie(dictList);
		
		// initialize values in dictTrie for new calculation		
		dictTrie.clearValues();
		dictTrie.getRoot().setValue(new ArrayList<Double>());
		while (dictTrie.getRoot().getValue().size() < m.length()) {
			dictTrie.getRoot().getValue().add(Double.POSITIVE_INFINITY);
		}
		
		editDistCalc(m, "", dictTrie.getRoot());
		List<Candidate> candidates = getRankedCandidates(dictTrie, window);
		
		return candidates;
	}

	public void editDistCalc(final String m, final String prefix, final Trie<List<Double>>.Node node) {

		// at root initialize first row of edit distance table
		if (node.getParent() == null) {
			node.getValue().set(0, 0.0);
			
			for (int i = 0; i < m.length(); i++) {
				String sstr = m.substring(0, i);
				//node.getValue().set(i, getProb(new Alignment(AlignmentUtils.nullString, sstr)));
				node.getValue().set(i, getProb(new Alignment(sstr, AlignmentUtils.nullString)));
			}
		}
		
		// initialize value of this node if necessary
		if (node.getValue() == null) {
			node.setValue(new ArrayList<Double>());
			while (node.getValue().size() < m.length()) {
				node.getValue().add(Double.POSITIVE_INFINITY);
			}
		}
		
		//node.getValue().set(0, getProb(new Alignment(prefix, AlignmentUtils.nullString)));
		node.getValue().set(0, getProb(new Alignment(AlignmentUtils.nullString, prefix)));

		for (int i = 1; i < m.length(); i++) {
			String sstr = prefix;
			String tstr = m.substring(0, i);
			double lowest = Double.POSITIVE_INFINITY;
			double e, e1, e2;

			node.getValue().set(i, Double.POSITIVE_INFINITY);
						
			for (int j = tstr.length(); j >= 0 /* && j > tstr.length() - window */; j--) {
					
				Trie<List<Double>>.Node pnode = node;
				String t2 = splitString(tstr, j);
				
				for (int k = sstr.length(); k >= 0 /* && k > sstr.length() - window */; k--) {
				
					String s2 =  splitString(sstr, k);

					if (pnode == null) {
						e1 = node.getValue().get(j);
					} else {
						e1 = pnode.getValue().get(j);
						pnode = pnode.getParent();
					}
					//e2 = getProb(new Alignment(s2, t2));
					e2 = getProb(new Alignment(t2, s2));
					System.out.println(new Alignment(t2, s2) + " " + e2);
					e = e1 + e2;
					if (e < lowest) {
						lowest = e;
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
	
	/* private void makeAlphaBetaTrie (ErrorModel e) {

		alphaBetaTrie = new Trie<Trie<Double>>();

		// first create the beta tries for each LHS
		Map<String, Trie<Double>> betaTries = new HashMap<String, Trie<Double>>();

		for (Map.Entry<Alignment, Double> a : e.getModel().entrySet()) {
			String lhs = a.getKey().lhs;
			String rhs = a.getKey().rhs;
			double prob = a.getValue();

			if (!betaTries.containsKey(lhs)) {
				betaTries.put(lhs, new Trie<Double>());
			}

			betaTries.get(lhs).put(StringUtils.reverse(rhs), prob);

		}

		// then create the whole alpha/beta trie
		for (String lhs : betaTries.keySet()) {
			alphaBetaTrie.put(StringUtils.reverse(lhs), betaTries.get(lhs));
		}
	}*/

	private void makeAlphaBetaMap(ErrorModel e) {

		alphaBetaMap = new HashMap<Alignment, Double>();

		for (Map.Entry<Alignment, Double> a : e.getModel().entrySet()) {
			final String lhs = a.getKey().lhs;
			final String rhs = a.getKey().rhs;
			final double prob = a.getValue();
			
			alphaBetaMap.put(new Alignment(lhs, rhs), prob);
		}
		
	}
	
	private Trie<List<Double>> makeDictTrie(Map<String, DictEntry> dict) {
		// create a trie for the dictionary
		Trie<List<Double>> dictTrie = new Trie<List<Double>>();

		for (DictEntry w : dict.values()) {
			//System.out.println(padWord(w, window));
			dictTrie.put(padWord(w.getWord(), window), new ArrayList<Double>());
		}
		
		return dictTrie;
	}

	private String padWord(String word, int window) {
		for (int i = 0; i < window; i++) {
			word = AlignmentUtils.leftPadding + word + AlignmentUtils.rightPadding;
		}
		
		return word;
	}
	
	private double getProb(Alignment a) {
		
		if (a.lhs.length() == 0) {
			a.lhs = AlignmentUtils.nullString;
		}
		if (a.rhs.length() == 0) {
			a.rhs = AlignmentUtils.nullString;
		}
		//System.out.print("Getting probability for: " + a + " ");
		
		if (alphaBetaMap.containsKey(a)) {
			return -Math.log(alphaBetaMap.get(a));
		}
		
		return Double.POSITIVE_INFINITY;
		
		/*
		
		// brute-search trie version
		
		Double prob = 0.0;
		
		Trie<Double> betaTrie = alphaBetaTrie.get(StringUtils.reverse(a.lhs));
		
		if (betaTrie == null) {
			return Double.POSITIVE_INFINITY;
		}
		
		prob = betaTrie.get(StringUtils.reverse(a.rhs));
		
		if (prob == null) {
			return Double.POSITIVE_INFINITY;
		}
		
		return -Math.log(prob);*/
	}
	
	/**
	 * Split string into two parts at the provided split position,
	 * replacing empty parts with the special null string.
	 * 
	 * @param word
	 * @param splitPos
	 * @return
	 */
	private String splitString(String word, int splitPos) {
		String s = word.substring(splitPos);

		if (s.length() == 0) {
			s = AlignmentUtils.nullString;
		}
		
		return s;
	}
	
	/**
	 * Read the probabilities for all candidates off the dictTrie and 
	 * return a ranked list of Candidates.
	 * 
	 * @param dictTrie
	 * @param window
	 * @return
	 */
	private List<Candidate> getRankedCandidates(Trie<List<Double>> dictTrie, int window) {
		List<Candidate> c = new ArrayList<Candidate>();
		Map<String, List<Double>> v = dictTrie.traverse(true);
		
		for (Map.Entry<String, List<Double>> p : v.entrySet()) {
			String candidate = p.getKey();
			Double prob = p.getValue().get(p.getValue().size() - 1);
			
			// TODO: incorporate dictionary probability as P(w) below
			c.add(new Candidate(candidate.substring(window, candidate.length() - window), prob));
		}
		
		Collections.sort(c);
		
		return c;
	}

}
