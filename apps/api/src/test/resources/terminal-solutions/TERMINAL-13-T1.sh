#!/usr/bin/env bash
set -euo pipefail
mkdir -p data
touch data/a.txt data/b.txt

cleanup() {
  echo "Nettoyage effectue"
  rm -rf archive
}
trap cleanup EXIT

archiver() {
  if [ "$#" -ne 1 ]; then return 2; fi
  if [ ! -d "$1" ]; then return 3; fi
  mkdir -p archive
  cp "$1"/* archive/
  echo "Archive: $(ls archive | wc -l) fichiers"
}

set +e
archiver
echo "Usage refuse: code $?"
archiver absent
echo "Dossier absent: code $?"
set -e
archiver data
