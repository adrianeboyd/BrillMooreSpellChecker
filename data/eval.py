#!/usr/bin/python3
"""
Usage: ./eval.py brillmoore-output.txt

Calculates spell checker scores similar to the Aspell evaluation tables:

http://aspell.net/test/common-all/

"""

import os
import sys

def eval_places(places, count):
  withincount = 0

  for p in places:
    if p > 0 and p <= count:
      withincount += 1

  return float(withincount) / len(places) * 100

def not_found(places):
  notfound = 0

  for p in places:
    if p < 0:
      notfound += 1

  return notfound

inputfileh = open(sys.argv[1])

total = 0
maxsuggestions = 0
places = []

for line in inputfileh:
  line = line.strip()
  lineparts = line.split("\t")

  misspelling = lineparts[0]
  correction = lineparts[1]
  count = int(lineparts[2])

  maxsuggestions = max(maxsuggestions, (len(lineparts) - 3) / 2)

  found = False
  for c in range(3, len(lineparts), 2):
    if lineparts[c] == correction:
      found = True

      for i in range(0, count):
        places.append(int((c - 1) / 2))

  if not found:
    for i in range(0, count):
      places.append(-1)
    print(misspelling + " " + correction)

  total += 1

header = "NotFnd\tFound\tFirst\t1-5\t1-10\t1-25\t1-50\tAny (Max: " + str(int(maxsuggestions)) + ")"
print(header)
print("-" * len(header.expandtabs()))
print("%d\t%d\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f" % (not_found(places), len(places) - not_found(places), eval_places(places, 1), eval_places(places, 5), eval_places(places, 10), eval_places(places, 25), eval_places(places, 50), eval_places(places, maxsuggestions)))
