package de.unituebingen.sfs.brillmoore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.unituebingen.sfs.brillmoore.aligner.Alignment;

public class SpellCheckerTest {

	@Test
	public void getRankedCandidates() throws ParseException {
		// TODO: expand test
		List<Misspelling> trainMisspellings = new ArrayList<Misspelling>();
		trainMisspellings.add(new Misspelling("Abril", "April", 1));
		
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		dict.put("April", new DictEntry("April", 1.0));
		dict.put("Arzt", new DictEntry("Arzt", 1.0));
		dict.put("Altstadt", new DictEntry("Altstadt", 1.0));
		
		SpellChecker spellcheckerWindow0 = new SpellChecker(trainMisspellings, dict, 0, 0.8);
		SpellChecker spellcheckerWindow3 = new SpellChecker(trainMisspellings, dict, 3, 0.8);
		
		Assert.assertEquals(spellcheckerWindow0.getRankedCandidates("Abril").get(0).getTarget(), "April");
		Assert.assertEquals(spellcheckerWindow0.getRankedCandidates("Abril").get(0).getProb(), -Math.log(0.2), 0.00001);
		Assert.assertEquals(spellcheckerWindow0.getRankedCandidates("Abril").get(1).getProb(), -Math.log(0.0), 0.00001);
		
		Assert.assertEquals(spellcheckerWindow3.getRankedCandidates("Abril").get(0).getTarget(), "April");
		Assert.assertEquals(spellcheckerWindow3.getRankedCandidates("Abril").get(0).getProb(), -Math.log(0.2), 0.00001);
		Assert.assertEquals(spellcheckerWindow3.getRankedCandidates("Abril").get(1).getProb(), -Math.log(0.0), 0.00001);
	}
	
	@Test(expectedExceptions = ParseException.class)
	public void getRankedCandidatesWithReservedDictionaryChars() throws ParseException {
		// TODO: expand test
		List<Misspelling> trainMisspellings = new ArrayList<Misspelling>();
		trainMisspellings.add(new Misspelling("Abril", "April", 1));
		
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		dict.put("A∀pril", new DictEntry("April", 1.0));
		dict.put("Arzt", new DictEntry("Arzt", 1.0));
		dict.put("Altstadt", new DictEntry("Altstadt", 1.0));
		
		SpellChecker spellcheckerWindow0 = new SpellChecker(trainMisspellings, dict, 0, 0.8);
	}
	
	@Test(expectedExceptions = ParseException.class)
	public void getRankedCandidatesWithReservedTrainingChars() throws ParseException {
		// TODO: expand test
		List<Misspelling> trainMisspellings = new ArrayList<Misspelling>();
		trainMisspellings.add(new Misspelling("A∀bril", "April", 1));
		
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		dict.put("April", new DictEntry("April", 1.0));
		dict.put("Arzt", new DictEntry("Arzt", 1.0));
		dict.put("Altstadt", new DictEntry("Altstadt", 1.0));
		
		SpellChecker spellcheckerWindow0 = new SpellChecker(trainMisspellings, dict, 0, 0.8);
	}
	
	@Test(expectedExceptions = ParseException.class)
	public void getRankedCandidatesWithReservedTestingChars() throws ParseException {
		// TODO: expand test
		List<Misspelling> trainMisspellings = new ArrayList<Misspelling>();
		trainMisspellings.add(new Misspelling("Abril", "April", 1));
		
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		dict.put("April", new DictEntry("April", 1.0));
		dict.put("Arzt", new DictEntry("Arzt", 1.0));
		dict.put("Altstadt", new DictEntry("Altstadt", 1.0));
		
		SpellChecker spellcheckerWindow0 = new SpellChecker(trainMisspellings, dict, 0, 0.8);
		
		spellcheckerWindow0.getRankedCandidates("A∀bril");
	}

}
