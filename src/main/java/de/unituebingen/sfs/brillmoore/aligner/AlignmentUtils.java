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

	/**
	 * Extends alignments with left and right context within window after 
	 * padding the alignments with the special null and left/right context
	 * alignments.
	 * 
	 * @param alignments alignments to extend
	 * @param window Brill and Moore's M
	 * @return
	 */
	public static List<Alignment> extendAlignments(List<Alignment> alignments, int window) {
		List<Alignment> padded = new ArrayList<Alignment>();
		List<Alignment> combined = new ArrayList<Alignment>();
		
		// add alignments to padded alignment list
		for (Alignment a : alignments) {
			padded.add(a);
		}
		
		// pad padded with padding strings for left and right context
		padded.add(0, new Alignment(leftPadding, leftPadding));
		padded.add(new Alignment(rightPadding, rightPadding));
		
		// pad padded with null string
		padded.add(0, new Alignment(nullString, nullString));
		padded.add(new Alignment(nullString, nullString));
		
		for (int i = 0; i < padded.size(); i++) {
			for (int j = 0; j <= window; j++) {
				int left = i - j;
				int right = i + j + 1;

				if (left >= 0 && i + 1 <= padded.size()) {
					combined.add(combineAlignments(padded.subList(left, i + 1)));
				}
				
				if (left != i && i + 1 != right) {
					if (i >= 0 && right <= padded.size()) {
						combined.add(combineAlignments(padded.subList(i, right)));
					}
				}
			}
		}
		
		return combined;
	}
}
