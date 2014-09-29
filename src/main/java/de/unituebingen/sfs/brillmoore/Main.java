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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

public class Main 
{

	public static void main(String[] argv) {
		String trainFile = null;
		String dictFile = null;
		String testFile = null;
		int window = 0;

		// create the command line parser
		CommandLineParser parser = new BasicParser();

		// create the Options
		Options options = new Options();

		options.addOption("p", "train", true, "training file");
		options.addOption("d", "dict", true, "dictionary file");
		options.addOption("t", "test", true, "testing file");
		options.addOption("w", "window", true, "window (Brill and Moore's N");
		options.addOption("h", "help", false, "print usage");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, argv);
			
			if (line.hasOption("help")) {
				printHelp(options);
			}

			trainFile = line.getOptionValue('p');
			dictFile = line.getOptionValue('d');
			testFile = line.getOptionValue('t');
			window = Integer.parseInt(line.getOptionValue('w'));
		} catch (org.apache.commons.cli.ParseException e) {
			System.out.println(e.getMessage());
			printHelp(options);
		}

		// read in files
		List<Misspelling> trainMisspellings = readMisspellings(trainFile);
		Map<String, DictEntry> dict = readDict(dictFile);
		List<Misspelling> testMisspellings = readMisspellings(testFile);
		
		// train spell checker
		SpellChecker spellchecker = new SpellChecker(trainMisspellings, dict, window);
		
		// call spell checker for each misspelling in test file
		for (Misspelling t : testMisspellings) {
			List<String> outList = new ArrayList<String>();
			outList.add(t.getSource());
			outList.add(t.getTarget());
			outList.add(Integer.toString(t.getCount()));
			
			List<Candidate> candidates = spellchecker.getRankedCandidates(t.getSource());

			for (Candidate cand : candidates.subList(0, Math.min(candidates.size(), 10))) {
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
	 * tab-separated: word, probability/count (not yet implemented)
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
	
	/**
	 * Print automatically-generated help and exit.
	 * 
	 * @param options
	 */
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("brillmoore", options);
		System.exit(0);
	}
}
