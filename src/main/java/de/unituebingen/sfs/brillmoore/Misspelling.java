package de.unituebingen.sfs.brillmoore;

public class Misspelling {
	private final String source;
    private final String target;
    private final int count;
    
    public Misspelling(String source, String target) {
    	this(source, target, 1);
    }

	public Misspelling(String source, String target, int count) {
        this.source = source;
        this.target = target;
        this.count = count;
    }

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public int getCount() {
		return count;
	}
}
