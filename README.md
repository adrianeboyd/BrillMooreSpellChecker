Brill and Moore Noisy Channel Spelling Correction
=================================================

This is an inefficient Java implementation of the noisy channel spelling
checking approach presented in:

Brill and Moore (2000). [An Improved Error Model for Noisy Channel Spelling
Correction](http://www.aclweb.org/anthology/P00-1037). In _Proceedings of the
ACL 2000_.

The spell checker's error model is trained on a list of misspellings with
corrections, considering generic character edits (e.g., the edit `ant`->`ent`
from the pair `dependant`->`dependent`) up to a specified maximum edit length.

To use this spell checker you need:

- a list of misspellings with corrections
- a list of potential corrections (i.e., a dictionary of real words)

Compiling and Running
---------------------

### Compile

```
$ mvn package
```

### Run

```
$ java -jar target/brillmoore-0.1-jar-with-dependencies.jar
```

### Usage

```
 -a,--minatoa <arg>   minimum a -> a probability (default 0.8)
 -c,--candidates      number of candidates to output (default 10)
 -d,--dict <arg>      dictionary file
 -h,--help            this help message
 -p,--train <arg>     training file
 -t,--test <arg>      testing file
 -w,--window <arg>    window for expanding alignments (Brill and Moore's
                      N; default 3)
```

### Maven

Install in the local maven archive:

```
$ mvn install
```

Add the maven dependency:

```
<dependency>
	<groupId>de.unituebingen.sfs</groupId>
	<artifactId>brillmoore</artifactId>
	<version>0.1</version>
</dependency>
```


Command Line Usage
------------------

Sample input files are provided in `data/`.

```
$ java -jar target/brillmoore-0.1-jar-with-dependencies.jar -a 0.7 \ 
  -c 5 -d data/sample-dict.txt -p data/sample-train.txt \ 
  -t data/sample-test.txt -w 2
```

Java Usage
----------

```
List<Misspelling> trainMisspellings = new ArrayList<Misspelling>();
trainMisspellings.add(new Misspelling("Abril", "April", 1));
		
Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
dict.put("April", new DictEntry("April", 1.0));
dict.put("Arzt", new DictEntry("Arzt", 1.0));
dict.put("Altstadt", new DictEntry("Altstadt", 1.0));

int window = 3;
double minAtoA = 0.8;
		
// train spell checker
SpellChecker spellchecker = new SpellChecker(trainMisspellings, dict, window, minAtoA);

// run spell checker
List<Candidate> candidates = spellchecker.getRankedCandidates("misspelling");

// iterate over top ten candidates
for (Candidate cand : candidates.subList(0, Math.min(candidates.size(), numCand))) {
	System.out.println(cand.getTarget() + "\t" + cand.getProb());
}

```

Input Formats
-------------

### Training/Testing

- counts are optional, assumed to be 1 if no count provided
- the test counts are merely copied into the output for further use

```
misspelling TAB target TAB count
```

### Dictionary

- without probabilities (one word per line, all words equally likely):

```
word
```

- with probabilities:

```
word TAB probability
```

Output
------

The output echoes the test input columns (misspelling, target, count) and 
appends the ranked candidate corrections as pairs of columns containing 
the candidate correction and the -log(prob) of the candidate.

```
misspelling TAB target TAB count TAB candidate1 TAB -log(prob1) TAB candidate2 TAB -log(prob2) ...
```
