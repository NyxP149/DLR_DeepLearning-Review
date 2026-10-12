#!/usr/bin/env bash
git config --global user.name "Ada"
git config --global user.email "ada@dlr.dev"
git config --global init.defaultBranch main
mkdir projet && cd projet
git init -q
printf 'l1\nl2\nl3\nl4\nl5\nl6\nl7\nl8\nl9\nl10\n' > app.py
echo 'DEBUG = False' > config.py
echo '# Projet' > README.md
git add -A && git commit -q -m "Initial"

printf 'l1\nl2\n' > app.py
echo 'API_KEY = "sk-live-1234"' >> config.py
echo 'TODO: documenter' >> README.md
git add -A

MAX_DELETIONS=5
echo "Fichiers modifiés: $(git diff --cached --name-only | wc -l)"
secrets=$(git grep --cached -lE 'API_KEY|sk-[a-z]+-[0-9]+' | xargs)
if [ -n "$secrets" ]; then echo "Secret détecté: oui ($secrets)"; else echo "Secret détecté: non"; fi
echo "TODO ajoutés: $(git diff --cached -U0 | grep -c '^+.*TODO')"
deleted=$(git diff --cached --numstat | awk '{d+=$2} END {print d+0}')
echo "Lignes supprimées: $deleted (seuil $MAX_DELETIONS)"
if [ -n "$secrets" ] || [ "$deleted" -gt "$MAX_DELETIONS" ]; then echo "Verdict: REFUSÉ"; else echo "Verdict: ACCEPTÉ"; fi
