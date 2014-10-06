package de.unituebingen.sfs.brillmoore.aligner;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AlignmentTest {

	@Test
	public void equals() {
		Alignment a = new Alignment("a", "b");
		Alignment b = new Alignment("a", "b");
		Alignment c = new Alignment("b", "b");
		Alignment d = new Alignment("b", "a");

		Assert.assertEquals(a, b);
		Assert.assertNotEquals(a, c);
		Assert.assertNotEquals(a, d);
	}
}
