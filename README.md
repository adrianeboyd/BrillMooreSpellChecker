Brill and Moore Noisy Channel Spelling Correction
=================================================

This is an inefficient* Java implementation of the noisy channel spelling
checking approach presented in:

Brill and Moore (2000). [An Improved Error Model for Noisy Channel Spelling
Correction](http://www.aclweb.org/anthology/P00-1037). In _Proceedings of the
ACL 2000_.

The spell checker's error model is trained on a list of pairs of misspellings
with corrections, considering generic character edits up to a specified maximum
edit length (e.g., the edit `ant`&rarr;`ent` from the pair
`dependant`&rarr;`dependent`).

To use this spell checker you need:

- a list of misspellings with corrections
- a list of potential corrections (i.e., a dictionary of real words)

*The &alpha; &rarr; &beta; alignment parameters are stored in a map rather than
a trie of tries, which slows down the algorithm considerably.

Command Line Usage
------------------

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

### Data Formats

Tab-separated values are used for input and output.

#### Training/Testing

- counts are optional, assumed to be 1 if no count provided
- the test counts are merely copied into the output for further use

```
misspelling TAB target TAB count
```

#### Dictionary

- without probabilities (one word per line, all words equally likely):

```
word
```

- with probabilities:

```
word TAB probability
```

#### Output

The output echoes the test input columns (misspelling, target, count) and
appends the ranked candidate corrections as pairs of columns containing the
candidate correction and the -log(prob) of the candidate.

```
misspelling TAB target TAB count TAB candidate1 TAB -log(prob1) TAB candidate2 TAB -log(prob2) ...
```

### Example

Sample input files are provided in `data/`.

```
$ java -jar target/brillmoore-0.1-jar-with-dependencies.jar -a 0.7 \ 
  -c 3 -d data/sample-dict.txt -p data/sample-train.txt \ 
  -t data/sample-test.txt -w 2
```

Sample output:

```
Abeit	Arbeit	1	Arbeit	1.2804152299663314	Adresse	Infinity	Alkohol	Infinity
Abril	April	1	April	1.2804152299663314	Adresse	Infinity	Alkohol	Infinity
Altstodt	Altstadt	1	Adresse	Infinity	Alkohol	InfinityAltstadt	Infinity
Arz	Arzt	1	Arzt	1.5035587812805413	Adresse	Infinity	Alkohol	Infinity
```


Java Usage
----------

```
// create a list of pairs of misspellings and corrections
List<Misspelling> trainMisspellings = new ArrayList<>();
trainMisspellings.add(new Misspelling("Abril", "April", 1));

// create a dictionary
Map<String, Double> dict = new HashMap<>();
dict.put("April", 1.0);
dict.put("Arzt", 1.0);
dict.put("Altstadt", 1.0);

// set the parameters
int window = 3;
double minAtoA = 0.8;

// train spell checker
try {
    SpellChecker spellchecker = new SpellChecker(trainMisspellings, dict, window, minAtoA);

    // run spell checker
    List<Candidate> candidates = spellchecker.getRankedCandidates("Abril");

    // iterate over top ten candidates
    for (Candidate cand : candidates.subList(0, Math.min(candidates.size(), 10))) {
        System.out.println(cand.getTarget() + "\t" + cand.getProb());
    }
} catch (ParseException e) {
    System.err.println(e.getMessage());
}

```

Using Maven
-----------

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

Credits
-------

This code includes modified versions of:

- [Trie](https://gist.github.com/rgantt/5711830) by Ryan Gantt ([further documentation](http://code.ryangantt.com/articles/introduction-to-prefix-trees/))
- [Damerau Levenshtein Algorithm](https://github.com/KevinStern/software-and-algorithms/blob/master/src/main/java/blogspot/software_and_algorithms/stern_library/string/DamerauLevenshteinAlgorithm.java) by Kevin L. Stern
