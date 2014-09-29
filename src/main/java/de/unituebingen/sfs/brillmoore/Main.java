package de.unituebingen.sfs.brillmoore;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Main 
{

	public static void main(String[] argv) {
		String trainFile = null;
		String dictFile = null;
		String testFile = null;

		int c;
		String arg;
		LongOpt[] longopts = new LongOpt[2];

		StringBuffer sb = new StringBuffer();
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("train", LongOpt.REQUIRED_ARGUMENT, sb, 'p');
		longopts[1] = new LongOpt("dict", LongOpt.REQUIRED_ARGUMENT, sb, 'd');
		longopts[1] = new LongOpt("test", LongOpt.REQUIRED_ARGUMENT, sb, 't');

		Getopt g = new Getopt("brillmoore", argv, "p:d:t:h", longopts);

		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'h':
				printHelp();
				break;

			case 'p':
				arg = g.getOptarg();
				trainFile = (arg != null) ? arg: null;
				break;

			case 'd':
				arg = g.getOptarg();
				dictFile = (arg != null) ? arg: null;
				break;

			case 't':
				arg = g.getOptarg();
				testFile = (arg != null) ? arg: null;
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
		
		if (trainFile == null || dictFile == null || testFile == null) {
			printHelp();
			System.exit(0);
		}

		List<Misspelling> trainMisspellings = readMisspellings(trainFile);
		Map<String, DictEntry> dict = readDict(dictFile);
		List<Misspelling> testMisspellings = readMisspellings(testFile);
		int window = 3;
		
		SpellChecker spellchecker = new SpellChecker(trainMisspellings, dict, window);
		
		for (Misspelling t : testMisspellings) {
			List<String> outList = new ArrayList<String>();
			outList.add(t.getSource());
			outList.add(t.getTarget());
			outList.add(Integer.toString(t.getCount()));
			
			List<Candidate> candidates = spellchecker.getRankedCandidates(t.getSource());

			for (Candidate cand : candidates.subList(0, Math.min(candidates.size(), 10))) {
				//System.out.print(cand.getTarget() + " " + cand.getProb());
				outList.add(cand.getTarget());
				outList.add(cand.getProb().toString());
			}
			
			System.out.println(StringUtils.join(outList, "\t"));
		}
	}

	/**
	 * Read in misspellings from file, tab-separated: source, target, count
	 * 
	 * @param file misspelling file
	 * @return
	 */
	private static List<Misspelling> readMisspellings(String file) {
		List<Misspelling> misspellings = new ArrayList<Misspelling>();
		
		// TODO: replace with a CSV reader for robustness?
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;
			int lineCount = 1;

			while ((line = input.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String source = null;
				String target = null;
				int count = 0;
				
				if (lineParts.length < 2) {
					input.close();
					throw new ParseException(line, lineCount);
				} else {
					source = lineParts[0];
					target = lineParts[1];
				}
				
				if (lineParts.length >= 3) {
					count = Integer.parseInt(lineParts[2]);
				}
				
				misspellings.add(new Misspelling(source, target, count));
				lineCount++;
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return misspellings;
	}
	
	/**
	 * Read in word list from from file, one word per line,
	 * tab-separated: word, probability (not yet implemented)
	 * 
	 * @param file word list file
	 * @return
	 */
	private static Map<String, DictEntry> readDict(String file) {
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		BufferedReader input;
		
		// TODO: add frequencies or probabilities to dict file
		try {
			input = new BufferedReader(new FileReader(file));
			String line;

			while ((line = input.readLine()) != null) {
				dict.put(line.trim(), new DictEntry(line.trim(), 1.0));
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	
	private static void printHelp() {
		System.out.println("Here is no help.");
	}
}
