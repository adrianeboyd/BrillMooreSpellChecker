package de.unituebingen.sfs.brillmoore;

public class Candidate implements Comparable<Candidate> {
    private final String target;
    private Double prob;

	public Candidate(String target, Double prob) {
        this.target = target;
        this.prob = prob;
    }

	public Double getProb() {
		return prob;
	}

	public void setProb(Double prob) {
		this.prob = prob;
	}

	public String getTarget() {
		return target;
	}

	public int compareTo(Candidate c) {
		return prob.compareTo(c.prob);
    }

	@Override
	public String toString() {
		return "Candidate [target=" + target + ", prob=" + prob + "]";
	}
}
