#!/usr/bin/env bash
mkdir -p vrac
touch vrac/rapport.pdf vrac/photo1.jpg vrac/photo2.jpg vrac/notes.txt vrac/donnees.csv vrac/build.sh vrac/LISEZMOI

for f in vrac/*; do
  [ -f "$f" ] || continue
  ext="${f##*.}"
  case "$ext" in
    pdf|txt) dest=docs ;;
    jpg) dest=images ;;
    csv) dest=data ;;
    sh) dest=scripts ;;
    *) dest=autres ;;
  esac
  mkdir -p "$dest"
  mv -n "$f" "$dest/"
done

total=0
for dir in autres data docs images scripts; do
  count=$(ls "$dir" | wc -l)
  echo "$dir: $count"
  total=$((total + count))
done
echo "Total: $total"
