Aspell Test Data
================

The Aspell common misspellings test data (`aspell-common.*`) was obtained from:

<http://aspell.net/test/common-all/>

The file <http://aspell.net/test/common-all/batch0.tab> was randomized and
split into:

* train (80%)
* dev (10%)
* test (10%)

The Aspell current/original test data (`aspell-current.all`) was obtained from:

<http://aspell.net/test/cur/batch0.tab>

Word Lists
==========

The word list `aspell-wordlist-en_USGBsGBz.70-1.txt` was generated here:

<http://app.aspell.net/create?max_size=70&spelling=US&spelling=GBs&spelling=GBz&max_variant=1&diacritic=strip&download=wordlist&encoding=utf-8&format=inline>

Note: be aware that this spell checker does not automatically consider
capitalized/lowercased versions of words in the word list, so for real use this
list would need to be extended with all possible capitalization patterns.

Word List Header
----------------

The word list file includes the following source and licensing
information:

```
Using Git Commit From: Sun Jan 22 17:39:13 2017 -0500 [fbc7107]

Copyright 2000-2014 by Kevin Atkinson

  Permission to use, copy, modify, distribute and sell these word
  lists, the associated scripts, the output created from the scripts,
  and its documentation for any purpose is hereby granted without fee,
  provided that the above copyright notice appears in all copies and
  that both that copyright notice and this permission notice appear in
  supporting documentation. Kevin Atkinson makes no representations
  about the suitability of this array for any purpose. It is provided
  "as is" without express or implied warranty.

Copyright (c) J Ross Beresford 1993-1999. All Rights Reserved.

  The following restriction is placed on the use of this publication:
  if The UK Advanced Cryptics Dictionary is used in a software package
  or redistributed in any form, the copyright notice must be
  prominently displayed and the text of this document must be included
  verbatim.

  There are no other restrictions: I would like to see the list
  distributed as widely as possible.

Special credit also goes to Alan Beale <biljir@pobox.com> as he has
given me an incredible amount of feedback and created a number of
special lists (those found in the Supplement) in order to help improve
the overall quality of SCOWL.

Many sources were used in the creation of SCOWL, most of them were in
the public domain or used indirectly.  For a full list please see the
SCOWL readme.

http://wordlist.aspell.net/
```
