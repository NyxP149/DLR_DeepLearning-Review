#!/usr/bin/env bash
mkdir -p projets/a projets/b projets/c projets/d
echo '{"scripts":{"test":"vitest"}}' > projets/a/package.json
echo '<project/>' > projets/b/pom.xml
echo '[tool.pytest]' > projets/c/pyproject.toml
touch projets/d/LISEZMOI

detect_test() {
  if [ -f "$1/package.json" ]; then echo "npm test"
  elif [ -f "$1/pom.xml" ]; then echo "mvn test"
  elif [ -f "$1/pyproject.toml" ]; then echo "pytest"
  else echo "(inconnu)"
  fi
}

for d in projets/*; do
  echo "$(basename "$d"): $(detect_test "$d")"
done

cat > projets/a/CLAUDE.md <<DOC
## Commandes
- Tests : $(detect_test projets/a)

## Architecture
- src : code, tests : tests unitaires

## Conventions
- Commits courts à l'impératif

## Interdits
- Ne jamais modifier les tests pour les faire passer
DOC

echo "Sections: $(grep '^## ' projets/a/CLAUDE.md | sed 's/^## //' | paste -sd, - | sed 's/,/, /g')"
if [ "$(wc -l < projets/a/CLAUDE.md)" -le 30 ]; then echo "Taille: ok"; else echo "Taille: trop long"; fi
if grep -qiE '(password|api[_-]?key|secret|token)[[:space:]]*[=:]' projets/a/CLAUDE.md; then echo "Secrets: détectés"; else echo "Secrets: aucun"; fi
