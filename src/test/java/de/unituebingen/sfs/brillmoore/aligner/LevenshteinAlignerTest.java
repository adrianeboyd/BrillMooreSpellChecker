package de.unituebingen.sfs.brillmoore.aligner;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LevenshteinAlignerTest {
	private LevenshteinAligner la;

	@BeforeMethod
	public void beforeMethod() {
		la = new LevenshteinAligner(1, 1, 1);
	}
	
	@Test
	public void getAlignments() {
		List<Alignment> l = la.getAlignments("abaa", "acbd");
		Assert.assertEquals(l.get(0), new Alignment("a", "a"));
		Assert.assertEquals(l.get(1), new Alignment("", "c"));
		Assert.assertEquals(l.get(2), new Alignment("b", "b"));
		Assert.assertEquals(l.get(3), new Alignment("a", "d"));
		Assert.assertEquals(l.get(4), new Alignment("a", ""));
	}

	@Test
	public void getDistance() {
		Assert.assertEquals(la.getDistance("abaa", "acbd"), 3);
		
		Assert.assertEquals(la.getDistance("", ""), 0);
		Assert.assertEquals(la.getDistance("ab", ""), 2);
		Assert.assertEquals(la.getDistance("", "a"), 1);
		Assert.assertEquals(la.getDistance("sitting", "kitten"), 3);
		Assert.assertEquals(la.getDistance("kitten", "sitting"), 3);
		Assert.assertEquals(la.getDistance("Saturday", "Sunday"), 3);
	}

	@Test
	public void getEdits() {
		List<String> l = la.getEdits("abaa", "acbd");
		Assert.assertEquals(l.get(0), LevenshteinAligner.EditOps.match);
		Assert.assertEquals(l.get(1), LevenshteinAligner.EditOps.insert);
		Assert.assertEquals(l.get(2), LevenshteinAligner.EditOps.match);
		Assert.assertEquals(l.get(3), LevenshteinAligner.EditOps.sub);
		Assert.assertEquals(l.get(4), LevenshteinAligner.EditOps.delete);
	}
}
