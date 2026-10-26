#!/usr/bin/env bash
mkdir -p prompts
cd prompts
printf '## But\nAjouter un test\n## Fichiers\nsrc/app.py\n## Limites\nSans dependance\n## Verification\npytest\n' > bon.md
printf '## But\nFaire mieux\n' > vague.md
cd ..
score() {
  local file="$1" found=0 missing=()
  for section in But Fichiers Limites Verification; do
    if grep -q "^## $section" "$file"; then
      found=$((found + 1))
    else
      missing+=("$section")
    fi
  done
  echo "$(basename "$file") : $found/4"
  for section in "${missing[@]}"; do
    echo "  manque : $section"
  done
  if [ "$found" -eq 4 ]; then echo "  Verdict : pret"; else echo "  Verdict : a completer"; fi
}
for f in prompts/bon.md prompts/vague.md; do score "$f"; done
