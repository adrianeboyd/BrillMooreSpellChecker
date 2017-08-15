Brill and Moore Noisy Channel Spelling Correction
=================================================

This is an inefficient* Java implementation of the noisy channel spell
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

This spell checker does not know anything about sentence-initial
capitalization, so it expects all possible forms of a word (capitalized,
lowercase, mixed case, etc.) to appear in the list of potential corrections.

*The &alpha; &rarr; &beta; alignment parameters are retrieved from a map rather
than a trie of tries, so the spell checker is currently quite slow, especially
for larger dictionaries. We hope to improve the efficiency in the future.

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

Sample input files based on the [Aspell common misspellings test
data](http://aspell.net/test/common-all/) are provided in `data/`. See
`data/README.md` for details.

```
$ java -jar target/brillmoore-0.1-jar-with-dependencies.jar -d data/aspell-wordlist-en_USGBsGBz.70-1.txt -p data/aspell-common.train -t data/aspell-common.dev.first10 -c 3 > data/aspell-common.dev.first10.USGBsGBz.70-1.out
```

Sample output:

```
pumkin  pumpkin 1       pumpkin 4.38    pumpkin's       6.67    bumkin  7.32
reorganision    reorganisation  1       reorganisation  2.88    reorganisation's        5.20    reorganisations 7.09
gallaxies       galaxies        1       galaxies        4.01    galaxy's        13.26   galaxy  17.45
superceeded     superseded      1       superseded      7.91    supersede       14.46   succeeded       18.34
millenia        millennia       1       millennia       2.11    millennial      6.23    millennial's    8.52
pseudonyn       pseudonym       1       pseudonym       4.69    pseudonym's     6.98    pseudonyms      8.87
synonymns       synonyms        1       synonyms        6.46    synonym's       8.29    synonym 12.49
prominant       prominent       1       predominant     1.76    prominent       2.71    preeminent      10.01
manouver        maneuver        1       maneuver        1.93    manoeuvre       3.76    maneuver's      4.27
obediance       obedience       1       obedience       1.98    obedience's     4.33    obeisance       10.12
```

Evaluation for sample output:

```
$ data/eval.py data/aspell-common.dev.first10.USGBsGBz.70-1.out
```

```
NotFnd	Found	First	1-5	1-10	1-25	1-50	Any (Max: 3)
--------------------------------------------------------------------
0	10	90.0	100.0	100.0	100.0	100.0	100.0
```

Evaluation for the whole dev set output in
`data/aspell-common.dev.USGBsGBz.70-1.out`:

```
NotFnd	Found	First	1-5	1-10	1-25	1-50	Any (Max: 100)
----------------------------------------------------------------------
18	403	84.1	93.1	94.8	95.5	95.7	95.7
```

(Compare to: <http://aspell.net/test/common-all/>)

Evaluation with default paramemeters training on all Aspell common misspellings
(`data/aspell-common.all`) and testing on Aspell current test data
(`data/aspell-current.all`), which focuses on difficult misspellings:

```
NotFnd	Found	First	1-5	1-10	1-25	1-50	Any (Max: 100)
----------------------------------------------------------------------
43	504	56.3	78.4	83.7	88.8	91.2	92.1
```

(Compare to: <http://aspell.net/test/cur/>)

_Note:_ some target corrections aren't found in the provided dictionary due to
capitalization issues (e.g., `The`, `muslims`) and run-on errors (`incase`).

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

try {
    // train spell checker
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

Output:

```
April	1.6094379124341005
Altstadt	Infinity
Arzt	Infinity
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
