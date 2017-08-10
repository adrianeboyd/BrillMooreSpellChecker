package de.unituebingen.sfs.brillmoore.aligner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlignmentUtilsTest {
	private List<Alignment> l;
	
	private int window = 1;
	
	@BeforeMethod
	public void beforeMethod() {
		l = new ArrayList<>();
		l.add(new Alignment("a", "b"));
		l.add(new Alignment("c", "d"));
	}

	@Test
	public void combineAlignments() {		
		Assert.assertEquals(AlignmentUtils.combineAlignments(l), new Alignment("ac", "bd"));
	}

	@Test
	public void extendAlignments() {
		List<Alignment> extendedAlignments = AlignmentUtils.extendAlignments(l, window);
		// extendedAlignments should be: 
		//   [∅ -> ∅, ∅∀ -> ∅∀, ∀ -> ∀, ∅∀ -> ∅∀, ∀a -> ∀b, a -> b, ∀a -> ∀b, ac -> bd, c -> d, 
		//    ac -> bd, c∃ -> d∃, ∃ -> ∃, c∃ -> d∃, ∃∅ -> ∃∅, ∅ -> ∅, ∃∅ -> ∃∅]

		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.nullString, AlignmentUtils.nullString)), 2);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.nullString + AlignmentUtils.leftPadding, AlignmentUtils.nullString + AlignmentUtils.leftPadding)), 2);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.leftPadding, AlignmentUtils.leftPadding)), 1);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.leftPadding + "a", AlignmentUtils.leftPadding + "b")), 2);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment("a", "b")), 1);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment("ac", "bd")), 2);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment("c", "d")), 1);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment("c" + AlignmentUtils.rightPadding, "d" + AlignmentUtils.rightPadding)), 2);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.rightPadding, AlignmentUtils.rightPadding)), 1);
		Assert.assertEquals(Collections.frequency(extendedAlignments, new Alignment(AlignmentUtils.rightPadding + AlignmentUtils.nullString, AlignmentUtils.rightPadding + AlignmentUtils.nullString)), 2);
	}
}