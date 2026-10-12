#!/usr/bin/env bash
mkdir -p tp && cd tp
mkdir -p notes
touch notes/a.txt notes/b.txt notes/c.log notes/tmp.tmp

mkdir -p archive/2024
cp notes/*.txt archive/2024/
mv notes/c.log archive/
rm notes/tmp.tmp
find . -type f | sort
