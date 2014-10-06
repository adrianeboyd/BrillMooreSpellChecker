package de.unituebingen.sfs.brillmoore.errormodel;

import java.util.HashMap;
import java.util.Map;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;

public class ErrorModel {

	private Map<Alignment, Integer> alignmentCounts;
	private double m;
	private Map<Alignment, Double> alignmentProbs;
	
	public ErrorModel(Map<Alignment, Integer> alignmentCounts, double m) {
		this.alignmentCounts = alignmentCounts;
		this.m = m;
		
		this.calculateModel();
	}
	
	private void calculateModel() {
		alignmentProbs = new HashMap<Alignment, Double>();
		
		Map<String, Integer> lhsCounts = new HashMap<String, Integer>();
		for (Map.Entry<Alignment, Integer> a : alignmentCounts.entrySet()) {
			// only expand alignments where alpha != beta if length > 1
			/*if (a.getKey().lhs.length() > 1 && a.getKey().lhs.equals(a.getKey().rhs)) {
				continue;
			}*/
			
			if (lhsCounts.containsKey(a.getKey().lhs)) {
				lhsCounts.put(a.getKey().lhs, lhsCounts.get(a.getKey().lhs) + a.getValue());
			} else {
				lhsCounts.put(a.getKey().lhs, a.getValue());
			}
		}
		
		for (Map.Entry<Alignment, Integer> a : alignmentCounts.entrySet()) {
			// only expand alignments where alpha != beta
			/*if (a.getKey().lhs.length() > 1 && a.getKey().lhs.equals(a.getKey().rhs)) {
				continue;
			}*/
			
			double prob = (1 - m) * (double) a.getValue() / (double) lhsCounts.get(a.getKey().lhs);

			if (a.getKey().lhs.equals(a.getKey().rhs)) {
				prob += m;
			}

			alignmentProbs.put(a.getKey(), prob);
		}
	}
	
	public Map<Alignment, Double> getModel() {
		return alignmentProbs;
	}
	
	public double getProb(Alignment a) {
		return alignmentProbs.get(a);
	}
	
	public String toString() {
		StringBuilder o = new StringBuilder();
		
		for (Map.Entry<Alignment, Double> a : alignmentProbs.entrySet()) {
			o.append(a.getKey() + "\t" + a.getValue() + "\n"); 
		}
		
		return o.toString();
	}
}
