#!/usr/bin/env bash
mkdir -p prompts
cat > prompts/vague.md <<'EOF'
Corrige le bug dans le code.
EOF
cat > prompts/bon.md <<'EOF'
## Objectif
Corriger l'arrondi de la TVA dans src/tva.py.
## Contexte
Le test tests/test_tva.py échoue sur 19,99.
## Contraintes
Ne pas modifier l'API publique ; aucune nouvelle dépendance.
## Critères d'acceptation
make test passe et le diff touche au plus 2 fichiers.
EOF

SECTIONS=("Objectif" "Contexte" "Contraintes" "Critères d'acceptation")

score() {
  local file=$1 found=0 missing=()
  for section in "${SECTIONS[@]}"; do
    if grep -qF "## $section" "$file"; then
      found=$((found + 1))
    else
      missing+=("$section")
    fi
  done
  echo "$(basename "$file") : $found/4"
  for section in "${missing[@]}"; do
    echo "  manque : $section"
  done
  if [ "$found" -eq 4 ]; then echo "  Verdict : prêt"; else echo "  Verdict : à compléter"; fi
}

for f in prompts/*.md; do score "$f"; done
