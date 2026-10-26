#!/usr/bin/env bash
mkdir -p tmp
cd tmp
emit() {
  echo out1
  echo out2
  echo out3
  echo err1 >&2
}
emit > out.txt 2> err.txt
echo "stdout: $(wc -l < out.txt) lignes"
echo "stderr: $(wc -l < err.txt) lignes"
emit > all.txt 2>&1
echo "fusion: $(wc -l < all.txt) lignes"
