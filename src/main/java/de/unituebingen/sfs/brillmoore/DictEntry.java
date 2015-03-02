package de.unituebingen.sfs.brillmoore;

public class DictEntry implements Comparable<DictEntry> {
    private final String word;
    private final Double prob;

	public DictEntry(String word, Double prob) {
        this.word = word;
        this.prob = prob;
    }
	
	public String getWord() {
		return word;
	}

	public Double getProb() {
		return prob;
	}

	public int compareTo(DictEntry c) {
		return word.compareTo(c.word);
    }
}
