#!/usr/bin/env bash
emit() {
  echo "ok 1"
  echo "erreur 1" >&2
  echo "ok 2"
  echo "erreur 2" >&2
}

emit > out.txt 2> err.txt
echo "stdout: $(wc -l < out.txt) lignes"
echo "stderr: $(wc -l < err.txt) lignes"
emit > tout.txt 2>&1
echo "fusion: $(wc -l < tout.txt) lignes"
