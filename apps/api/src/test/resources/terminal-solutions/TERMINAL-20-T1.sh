#!/usr/bin/env bash
mkdir -p projets/a projets/b projets/c projets/d
touch projets/a/package.json projets/b/pom.xml projets/c/go.mod
detect_test() {
  if [ -f "$1/package.json" ]; then echo "npm test"
  elif [ -f "$1/pom.xml" ]; then echo "mvn test"
  elif [ -f "$1/go.mod" ]; then echo "go test ./..."
  else echo "(inconnu)"; fi
}
for p in a b c d; do
  echo "$p: $(detect_test projets/$p)"
done
cmd="$(detect_test projets/a)"
cat > projets/a/CLAUDE.md <<EOF
# Projet a

## Commandes
- Tests : $cmd

## Architecture
- src/ contient le code, tests/ les tests.

## Conventions
- Un commit par changement logique.

## Interdits
- Ne jamais committer .env.
EOF
echo "Sections: $(grep '^## ' projets/a/CLAUDE.md | sed 's/^## //' | paste -sd, | sed 's/,/, /g')"
if [ "$(wc -l < projets/a/CLAUDE.md)" -le 30 ]; then echo "Taille: ok"; else echo "Taille: trop long"; fi
if grep -qiE 'password|token|sk-' projets/a/CLAUDE.md; then echo "Secrets: detectes"; else echo "Secrets: aucun"; fi
