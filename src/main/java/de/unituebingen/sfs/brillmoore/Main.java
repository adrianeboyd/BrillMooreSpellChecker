package de.unituebingen.sfs.brillmoore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

public class Main 
{
	private static Options options;

	public static void main(String[] argv) {
		String trainFile = null;
		String dictFile = null;
		String testFile = null;
		int window = 3;
		double minAtoA = 0.8;
		int numCand = 10;

		// create the command line parser
		CommandLineParser parser = new BasicParser();

		// create the Options
		options = new Options();

		options.addOption("p", "train", true, "training file");
		options.addOption("d", "dict", true, "dictionary file");
		options.addOption("t", "test", true, "testing file");
		options.addOption("w", "window", true, "window for expanding alignments (Brill and Moore's N; default 3)");
		options.addOption("a", "minatoa", true, "minimum a -> a probability (default 0.8)");
		options.addOption("c", "candidates", true, "number of candidates to output (default 10)");
		options.addOption("h", "help", false, "this help message");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, argv);
			
			if (line.hasOption("help")) {
				printHelp();
			}

			trainFile = line.getOptionValue('p');
			dictFile = line.getOptionValue('d');
			testFile = line.getOptionValue('t');
			if (line.hasOption('w')) {
				try {
					window = Integer.parseInt(line.getOptionValue('w'));
				} catch (NumberFormatException e) {
					System.out.println("The window (-w) option was not formatted as an integer.");
					printHelp();
				}
			}
			
			if (line.hasOption('a')) {
				try {
					minAtoA = Double.parseDouble(line.getOptionValue('a'));
				} catch (NumberFormatException e) {
					System.out.println("The alignment (-a) option was not formatted as a float.");
					printHelp();
				}
			}
			
			if (line.hasOption('c')) {
				try {
					numCand = Integer.parseInt(line.getOptionValue('c'));
				} catch (NumberFormatException e) {
					System.out.println("The candidate (-c) option was not formatted as an integer.");
					printHelp();
				}
			}
		} catch (org.apache.commons.cli.ParseException e) {
			System.out.println(e.getMessage());
			printHelp();
		}
		
		// check that file parameters are given
		if (trainFile == null) {
			System.out.println("Please specify a training file (-p).");
			printHelp();
		}
		if (dictFile == null) {
			System.out.println("Please specify a dictionary file (-d).");
			printHelp();
		}
		if (testFile == null) {
			System.out.println("Please specify a testing file (-t).");
			printHelp();
		}

		// check that parameters are within sensible ranges
		if (window < 0) {
			System.out.println("The window (-w) for expanding alignments must be 0 or greater.");
			printHelp();
		}
		
		if (minAtoA < 0 || minAtoA > 1) {
			System.out.println("The minimum a -> a probability (-a) must be between 0 and 1.");
			printHelp();
		}
		
		if (numCand <= 0) {
			System.out.println("The number of candidates (-c) to output must be greater than 0.");
			printHelp();
		}

		// read in files
		List<Misspelling> trainMisspellings = readMisspellings(trainFile);
		Map<String, DictEntry> dict = readDict(dictFile);
		List<Misspelling> testMisspellings = readMisspellings(testFile);
		
		// train spell checker
		SpellChecker spellchecker = new SpellChecker(trainMisspellings, dict, window, minAtoA);
		
		// call spell checker for each misspelling in test file
		for (Misspelling t : testMisspellings) {
			List<String> outList = new ArrayList<String>();
			outList.add(t.getSource());
			outList.add(t.getTarget());
			outList.add(Integer.toString(t.getCount()));
			
			List<Candidate> candidates = spellchecker.getRankedCandidates(t.getSource());

			for (Candidate cand : candidates.subList(0, Math.min(candidates.size(), numCand))) {
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
				int count = 1;
				
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
			System.err.println("The file " + file + " could not be opened.");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("The file " + file + " could not be read.");
			System.exit(-1);
		} catch (ParseException e) {
			System.err.println("The file " + file + " could not be parsed at line " + e.getErrorOffset() + ".  The format is: \n" +
					"misspelling TAB target TAB count");
			System.exit(-1);
		}
		
		return misspellings;
	}
	
	/**
	 * Read in word list from from file, one word per line,
	 * tab-separated: word, probability/count (not yet implemented)
	 * 
	 * @param file word list file
	 * @return
	 */
	private static Map<String, DictEntry> readDict(String file) {
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		BufferedReader input;
		
		try {
			input = new BufferedReader(new FileReader(file));
			String line;
			int lineCount = 1;

			while ((line = input.readLine()) != null) {

				String[] lineParts = line.split("\t");
				String word = null;
				double freq = 0;

				if (lineParts.length >= 1) {
					word = lineParts[0];
					freq = 1.0;
				} else if (lineParts.length == 2) {
					freq = Double.parseDouble(lineParts[1]);
				} else {
					input.close();
					throw new ParseException(line, lineCount);
				}
				
				dict.put(word, new DictEntry(word, freq));
				lineCount++;
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			System.err.println("The file " + file + " could not be opened.");
			printHelp();
		} catch (IOException e) {
			System.err.println("The file " + file + " could not be read.");
			printHelp();
		} catch (ParseException e) {
			System.err.println("The file " + file + " could not be parsed at line " + e.getErrorOffset() + ".  The format is: \n" +
					"word TAB probability");
			printHelp();
		}
		
		return dict;
	}
	
	/**
	 * Print automatically-generated help and exit.
	 * 
	 * @param options
	 */
	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar brillmoore-0.0.1-SNAPSHOT-jar-with-dependencies.jar", options);
		System.exit(0);
	}
}
