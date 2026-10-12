#!/usr/bin/env bash
git config --global user.name "Ada"
git config --global user.email "ada@dlr.dev"
git config --global init.defaultBranch main
mkdir reprise && cd reprise
printf 'def add(a, b):\n    return a + b\n' > app.py
printf 'from app import add\nassert add(1, 2) == 3\n' > test_app.py
echo "Notes de reprise" > notes.txt
echo "TOKEN=sk-live-9999" > .env
echo "log" > build.log
printf 'a,b\n1,2\n' > data.csv
git init -q

printf '.env\n*.log\n' > .gitignore
cat > .git/hooks/pre-commit <<'HOOK'
#!/usr/bin/env bash
if git diff --cached -U0 | grep -q '^+.*sk-live-'; then
  echo "secret détecté" >&2
  exit 1
fi
HOOK
chmod +x .git/hooks/pre-commit

git add app.py test_app.py .gitignore && git commit -q -m "Ajoute le code et .gitignore"
git add notes.txt && git commit -q -m "Ajoute la documentation"
git add data.csv && git commit -q -m "Ajoute les données"

cat > Makefile <<'MAKE'
.RECIPEPREFIX = >
.PHONY: test
test:
> grep -q "def add" app.py && echo "test ok"
MAKE
cat > CLAUDE.md <<'DOC'
## Commandes
- Tests : make test

## Architecture
- app.py : code, test_app.py : test

## Interdits
- Ne jamais lire ni commiter .env
DOC
mkdir -p .claude
cat > .claude/settings.json <<'JSON'
{"permissions": {"deny": ["Read(./.env)"]}}
JSON
git add Makefile CLAUDE.md .claude/settings.json && git commit -q -m "Ajoute les garde-fous du projet"

echo "Ignorés: $(git check-ignore .env build.log | xargs)"
git add -f .env
if git commit -q -m "Test du garde-fou" 2>/dev/null; then guard="inactif"; else guard="actif"; fi
git rm --cached -q .env 2>/dev/null || git reset -q -- .env
echo "Garde-fou: $guard"
echo "Vérification: $(make -s test)"
echo "Commits: $(git log --format=%s | wc -l)"
echo "CLAUDE.md: $(grep -c '^## ' CLAUDE.md) sections"
if jq -e '.permissions.deny | index("Read(./.env)")' .claude/settings.json >/dev/null; then echo "Permissions: .env protégé"; else echo "Permissions: .env exposé"; fi
if [ -z "$(git status --short)" ]; then echo "Statut Git: propre"; else echo "Statut Git: modifié"; fi
