package de.unituebingen.sfs.brillmoore;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ryangantt.util.Trie;
import com.ryangantt.util.Trie.Node;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;
import de.unituebingen.sfs.brillmoore.aligner.AlignmentUtils;
import de.unituebingen.sfs.brillmoore.aligner.LevenshteinAligner;
import de.unituebingen.sfs.brillmoore.errormodel.ErrorModel;

public class Spellchecker 
{
	private Node node;

	public static void main(String[] argv) {
		String pairsFile = null;
		String dictFile = null;

		int c;
		String arg;
		LongOpt[] longopts = new LongOpt[2];

		StringBuffer sb = new StringBuffer();
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("pairs", LongOpt.REQUIRED_ARGUMENT, sb, 'p');
		longopts[1] = new LongOpt("dict", LongOpt.REQUIRED_ARGUMENT, sb, 'p');

		Getopt g = new Getopt("brillmoore", argv, "p:d:h", longopts);

		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'h':
				System.out.println("Here is no help.");
				break;

			case 'p':
				arg = g.getOptarg();
				pairsFile = (arg != null) ? arg: null;
				break;

			case 'd':
				arg = g.getOptarg();
				dictFile = (arg != null) ? arg: null;
				break;
				
			case ':':
				System.out.println("Provide an argument for option " +
						(char) g.getOptopt());
				break;
			case '?':
				System.out.println("The option '" + (char)g.getOptopt() + 
						"' is not valid");
				break;
			default:
				System.out.println("getopt() returned " + c);
				break;
			}
		}

		List<Misspelling> misspellings = readMisspellingPairs(pairsFile);
		List<String> dict = readDict(dictFile);
		int window = 3;
		
		Map<Alignment, Integer> alignmentCounts = new HashMap<Alignment, Integer>();

		LevenshteinAligner la = new LevenshteinAligner(1, 1, 1);
		
		for (Misspelling m : misspellings) {
			String source = m.getSource();
			String target = m.getTarget();
			int count = m.getCount();
			List<Alignment> alignments = la.getAlignments(source, target);
			List<Alignment> expandedAlignments = AlignmentUtils.extendAlignments(alignments,  window);
			
			for (Alignment a : expandedAlignments) {
				Integer prevCount = alignmentCounts.get(a);

				if (prevCount == null) {
					prevCount = 0;
				}
				
				alignmentCounts.put(a, prevCount + count);
			}
		}
		
		// generate an error model from the alignment counts, with a default
		// minimum probabilty for alpha -> alpha (m from my thesis, p. 24)
		ErrorModel e = new ErrorModel(alignmentCounts, 0.8);
		
		// create the alpha/beta trie with reversed strings
		Trie<Trie<Double>> alphaBetaTrie = 	makeAlphaBetaTrie(e);
		
		// create a trie for the dictionary
		Trie<Double> dictTrie = new Trie<Double>();
		
		for (String w : dict) {
			dictTrie.put(padWord(w, window), 0.0);
		}

		// traverse the dictionary trie to calculate the edit distance between 
		// a misspelling and all words in the dictionary
		String misspelling = padWord("Freizet", window);
		
/*

// Calculate the edit distance between the provided word and each word
// in the tree
void DTree::editDist(char word[MAX_WORD_LENGTH], ATree *aTree, ofstream& file)
{
	int i;
        char string[MAX_WORD_LENGTH];

	for(i = 0; i < MAX_ALPHA; i++)
	{
		strcpy(string, "");
		editDistCalc(root[i], word, string, 0, aTree, file);
	}
}

// Recursively go through the entire tree to calculate the edit distance
void DTree::editDistCalc(DNode *node, char word[MAX_WORD_LENGTH], char str[MAX_WORD_LENGTH], int depth, ATree *aTree, ofstream& file)
{
	int i, j, k;
	char tstr[MAX_WORD_LENGTH];
	char sstr[MAX_WORD_LENGTH];
	char s1[MAX_WORD_LENGTH];
	char s2[MAX_WORD_LENGTH];
	char t1[MAX_WORD_LENGTH];
	char t2[MAX_WORD_LENGTH];
	float lowest, e, e1, e2;
	DNode *tempnode, *pnode;

	if(node == NULL)
	{
		return;
	}
	// add the current character to the partial word string
	str[depth] = (*node).getLetter();

	// terminate the partial word string
	str[depth+1] = '\0';

	// if we're at the top row, all are 0 -> xxx
	if((*node).getParentNode() == NULL)
	{
		edit[0] = 0.0;

		for(i = 0; i < strlen(word); i++)
		{
			sstr[i] = word[i];
			sstr[i+1] = '\0';
			//cout << 0 << " " << sstr << " " << (*aTree).find("0", sstr) << endl;
			edit[i+1] = (*aTree).find("0", sstr);
		}
	}

	// first column in current row is str -> 0
	(*node).setEdit(0, (*aTree).find(str, "0"));

	// for the current row, finish the rest of the columns (none include
	// 0)
	//
	// sstr: string for current position in dict trie
	// tstr: target string

	for(i = 1; i <= strlen(word); i++)
	{
		strcpy(sstr, str);
		strcpy(tstr, word);

		tstr[i] = '\0';
		lowest = INF;
		(*node).setEdit(i, INF);
		for(j = strlen(tstr); j >= 0 && j > strlen(tstr) - 5; j--)
		{
			strcpy(sstr, str);
			substring(tstr, t1, t2, j);
			pnode = node;
			for(k = strlen(sstr); k >= 0 && k > strlen(sstr) - 5; k--)
			{
				substring(sstr, s1, s2, k);
				if(pnode == NULL)
				{
					e1 = edit[j];
				}
				else
				{
					e1 = (*pnode).getEdit(j);
					pnode = (*pnode).getParentNode();
				}
				e2 = (*aTree).find(s2, t2);

				e = e1 + e2;
				if(e < lowest)
				{
					lowest = e;
				}
			}
					
		}
		(*node).setEdit(i, lowest);
	}

	if((*node).isFinal())
	{
		cout << str << "\t" << (*node).getEdit(strlen(word)) << endl;
	}

	// repeat for each of the children of this node
	for(i = 0; i < MAX_ALPHA; i++)
	{
		tempnode = (*node).getChildNode(indexToAlpha(i));
		if(tempnode != NULL)
		{
			editDistCalc(tempnode, word, str, depth+1, aTree, file);
		}
	}

}
*/
		
	}
	
	public static Map<String, Double> editDist(String m, Trie<Trie<Double>> abt, Trie<Double> dict) {
		Map<String, Double> editDistances = new HashMap<String, Double>();
		
		editDistCalc(dict, m, "", dict.getRoot());
		
		return editDistances;
	}

	public static void editDistCalc(Trie<Double> dict, final String m, final String prefix, final Trie<Double>.Node node) {
		// at a terminated node
		if (null != node.getValue()) {
			// list.add(prefix + " " + node.getValue());
			// save something in dict
		}

		// traverse the children
		for (final Trie<Double>.Node child : node.getChildren()) {
			if ((null != child) && (null != child.getKey())) {
				editDistCalc(dict, m, prefix + child.getKey(), child);
			}
		}
		
		//return list;
	}
	
	private static Trie<Trie<Double>> makeAlphaBetaTrie (ErrorModel e) {

		Trie<Trie<Double>> alphaBetaTrie = new Trie<Trie<Double>>();

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
		
		// move to AlignmentUtils or as part of ErrorModel?
		betaTries.put(AlignmentUtils.leftPadding, new Trie<Double>());
		betaTries.get(AlignmentUtils.leftPadding).put(AlignmentUtils.rightPadding, 1.0);

		// then create the whole alpha/beta trie
		for (String lhs : betaTries.keySet()) {
			alphaBetaTrie.put(StringUtils.reverse(lhs), betaTries.get(lhs));
		}
		
		// ditto?
		alphaBetaTrie.put(AlignmentUtils.leftPadding, betaTries.get(AlignmentUtils.leftPadding));

		return alphaBetaTrie;
	}


	/**
	 * Read in misspellings from file, tab-separated: source, target, count
	 * 
	 * @param file misspelling file
	 * @return
	 */
	private static List<Misspelling> readMisspellingPairs(String file) {
		List<Misspelling> misspellings = new ArrayList<Misspelling>();
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;

			while ((line = input.readLine()) != null) {
				String[] lineParts = line.split("\t");
				misspellings.add(new Misspelling(lineParts[0], lineParts[1], Integer.parseInt(lineParts[2])));
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return misspellings;
	}
	
	/**
	 * Read in word list from from file, one word per line.
	 * 
	 * @param file word list file
	 * @return
	 */
	private static List<String> readDict(String file) {
		List<String> dict = new ArrayList<String>();
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;

			while ((line = input.readLine()) != null) {
				dict.add(line.trim());
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	
	private static String padWord(String word, int window) {
		for (int i = 0; i < window; i++) {
			word = AlignmentUtils.leftPadding + word + AlignmentUtils.rightPadding;
		}
		
		return word;
	}
}
