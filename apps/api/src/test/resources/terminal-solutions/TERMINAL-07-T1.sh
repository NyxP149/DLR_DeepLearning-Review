#!/usr/bin/env bash
mkdir -p vrac
cd vrac
touch notes.md todo.txt logo.png photo.jpg export.json backup run.sh
cd ..
mkdir -p tri/docs tri/img tri/data tri/autres
for f in vrac/*; do
  name="$(basename "$f")"
  case "$name" in
    *.md|*.txt) dest=docs ;;
    *.png|*.jpg) dest=img ;;
    *.json|*.csv) dest=data ;;
    *) dest=autres ;;
  esac
  mv -n "$f" "tri/$dest/"
done
total=0
for d in autres data docs img; do
  n=$(ls "tri/$d" | wc -l)
  echo "$d: $n"
  total=$((total + n))
done
echo "Total: $total"
