package de.unituebingen.sfs.brillmoore.aligner;

import java.util.ArrayList;
import java.util.List;

public class AlignmentUtils {
	// rare characters for padding alignments at the beginning and ends of words
	public static String leftPadding = "\u2200";
	public static String rightPadding = "\u2203";
	
	// rare character for null string
	public static String nullString = "\u2205";
	
	/**
	 * Concatenates the left-hand side and right-hand side of a list
	 * of alignment rules to generate a single combined rule, e.g.,
	 * a -> b, c -> d becomes ac -> bd.
	 * 
	 * @param alignments a list of Alignment rules
	 * @return a single alignment with concatenated left- and right-hand sides
	 */
	public static Alignment combineAlignments(List<Alignment> alignments) {
		StringBuilder clhs = new StringBuilder();
		StringBuilder crhs = new StringBuilder();
		
		for (Alignment a: alignments) {
			clhs.append(a.lhs);
			crhs.append(a.rhs);
		}
		
		if (clhs.length() == 0) {
			clhs = new StringBuilder(nullString);
		}
		if (crhs.length() == 0) {
			crhs = new StringBuilder(nullString);
		}
		
		return new Alignment(clhs.toString(), crhs.toString());
	}

	/* window is Brill and Moore's M */
	public static List<Alignment> extendAlignments(List<Alignment> alignments, int window) {
		List<Alignment> combined = new ArrayList<Alignment>();
		for (int i = 0; i < window; i++) {
			alignments.add(0, new Alignment(leftPadding, leftPadding));
			alignments.add(new Alignment(rightPadding, rightPadding));
		}
		
		for (int i = window; i < alignments.size() - window; i++) {
			for (int j = 0; j <= window; j++) {
				int left = i - j;
				int right = i + j + 1;
				
				combined.add(combineAlignments(alignments.subList(left, i + 1)));
				combined.add(combineAlignments(alignments.subList(i, right)));
			}
		}
		
		return combined;
	}
}
