package de.unituebingen.sfs.brillmoore.errormodel;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;

public class ErrorModelTest {
	private double minAtoA = 0.8;
	
	@Test
	public void calculateModelAndGetProb() {
		Map<Alignment, Integer> alignmentCounts = new HashMap<>();
		alignmentCounts.put(new Alignment("a", "b"), 1);
		alignmentCounts.put(new Alignment("a", "a"), 9);
		alignmentCounts.put(new Alignment("c", "d"), 2);
		alignmentCounts.put(new Alignment("c", "c"), 8);
		ErrorModel e = new ErrorModel(alignmentCounts, minAtoA);
		
		Assert.assertEquals(e.getProb(new Alignment("a", "b")), (1.0 - minAtoA) * 0.1);
		Assert.assertEquals(e.getProb(new Alignment("a", "a")), minAtoA + (1.0 - minAtoA) * 0.9);
		Assert.assertEquals(e.getProb(new Alignment("c", "d")), (1.0 - minAtoA) * 0.2);
		Assert.assertEquals(e.getProb(new Alignment("c", "c")), minAtoA + (1.0 - minAtoA) * 0.8);
		Assert.assertEquals(e.getProb(new Alignment("a", "f")), 0.0);
	}
}
