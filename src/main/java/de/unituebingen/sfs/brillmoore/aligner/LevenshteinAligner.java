package de.unituebingen.sfs.brillmoore.aligner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* Copyright (c) 2012 Kevin L. Stern
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * The Levenshtein Algorithm which solves the edit distance problem between a 
 * source string and a target string with the following operations:
 * 
 * <ul>
 * <li>Character Insertion</li>
 * <li>Character Deletion</li>
 * <li>Character Replacement</li>
 * </ul>
 *
 * <p>
 * 
 * This implementation allows the client to specify the costs of the various
 * edit operations.
 * 
 * <p>
 * 
 * The running time of the Levenshtein algorithm is O(n*m) where n is
 * the length of the source string and m is the length of the target string.
 * This implementation consumes O(n*m) space.
 * 
 * @author Kevin L. Stern
 * @author Adriane Boyd
 */
public class LevenshteinAligner {
	private final int deleteCost, insertCost, replaceCost;
	
	public class EditOps {
		public final static String delete = "DEL";
		public final static String insert = "INS";
		public final static String match = "MAT";
		public final static String sub = "SUB";
	}

	/**
	 * Constructor.
	 * 
	 * @param deleteCost
	 *          the cost of deleting a character.
	 * @param insertCost
	 *          the cost of inserting a character.
	 * @param replaceCost
	 *          the cost of replacing a character.
	 */
	public LevenshteinAligner(int deleteCost, int insertCost,
			int replaceCost) {
		this.deleteCost = deleteCost;
		this.insertCost = insertCost;
		this.replaceCost = replaceCost;
	}

	/**
	 * Compute the Levenshtein distance between the specified source
	 * string and the specified target string.
	 */
	public List<String> getEdits(String source, String target) {	
		int[][] table = new int[source.length()][target.length()];
		String[][] backtrace = new String[source.length()][target.length()];
		
		if (source.charAt(0) != target.charAt(0)) {
			table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
			backtrace[0][0] = EditOps.sub;
		} else {
			table[0][0] = 0;
			backtrace[0][0] = EditOps.match;
		}
		
		for (int i = 1; i < source.length(); i++) {
			int deleteDistance = table[i - 1][0] + deleteCost;
			int insertDistance = (i + 1) * deleteCost + insertCost;
			int matchDistance = i * deleteCost
					+ (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
			table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
			backtrace[i][0] = EditOps.delete;
		}
		
		for (int j = 1; j < target.length(); j++) {
			int deleteDistance = (j + 1) * insertCost + deleteCost;
			int insertDistance = table[0][j - 1] + insertCost;
			int matchDistance = j * insertCost
					+ (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
			table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
					matchDistance);
			backtrace[0][j] = EditOps.insert;
		}

		for (int i = 1; i < source.length(); i++) {
			for (int j = 1; j < target.length(); j++) {
				int deleteDistance = table[i - 1][j] + deleteCost;
				int insertDistance = table[i][j - 1] + insertCost;
				int matchDistance = table[i - 1][j - 1];
				if (source.charAt(i) != target.charAt(j)) {
					matchDistance += replaceCost;
				}
				table[i][j] = Math.min(Math
						.min(deleteDistance, insertDistance), matchDistance);
				
				
				if (table[i][j] == deleteDistance) {
					backtrace[i][j] = EditOps.delete;
				} else if (table[i][j] == insertDistance) {
					backtrace[i][j] = EditOps.insert;
				} else {
					if (source.charAt(i) == target.charAt(j)) {
						backtrace[i][j] = EditOps.match;
					} else {
						backtrace[i][j] = EditOps.sub;
					}
				}
			}
		}
		
		// backtrace through matrix
		List<String> reverseEdits = new ArrayList<String>();
		
		int posi = source.length() - 1;
		int posj = target.length() - 1;
		
		while(posi >= 0 && posj >= 0) {
			String edit = backtrace[posi][posj];
			reverseEdits.add(edit);
			if (edit == EditOps.match || edit == EditOps.sub) {
				posi--;
				posj--;
			} else if (edit == EditOps.insert) {
				posj--;
			} else if (edit == EditOps.delete) {
				posi--;
			}
		}
		
		Collections.reverse(reverseEdits);
		return reverseEdits; // not reversed here!
	
		//return table[source.length() - 1][target.length() - 1];
	}
	
	public List<Alignment> getAlignments(String source, String target) {
		List<String> edits = getEdits(source, target);
		List<Alignment> alignments = new ArrayList<Alignment>();
		
		int posi = 0;
		int posj = 0;
		for(int i = 0; i < edits.size(); i++) {
			String edit = edits.get(i);
			if (edit == EditOps.match || edit == EditOps.sub) {
				alignments.add(new Alignment(String.valueOf(source.charAt(posi)), String.valueOf(target.charAt(posj))));
				posi++;
				posj++;
			} else if (edit == EditOps.insert) {
				alignments.add(new Alignment("", String.valueOf(target.charAt(posj))));
				posj++;
			} else if (edit == EditOps.delete) {
				alignments.add(new Alignment(String.valueOf(source.charAt(posi)), ""));
				posi++;
			}
		}
		
		return alignments;
	}
	
	public int getDistance(String source, String target) {
		List<String> edits = getEdits(source, target);
		int dist = 0;
		for (String edit : edits) {
			if (edit != EditOps.match) {
				dist++;
			}
		}
		
		return dist;
	}
	
	/* public List<Alignment> getAlignments {
		
	}*/
}
