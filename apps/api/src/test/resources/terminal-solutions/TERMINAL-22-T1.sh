#!/usr/bin/env bash
mkdir -p revue
cd revue
git init -q -b main
git config user.name "Eleve"
git config user.email "eleve@dlr.dev"
printf 'a = 1\nb = 2\nc = 3\n' > config.py
seq 1 12 > lib.py
git add .
git commit -q -m "Base"
printf 'a = 1\nb = 2\nc = 3\npassword = "hunter2"\n# TODO a changer\n' > config.py
seq 1 4 > lib.py
git add .
files=$(git diff --cached --name-only | wc -l)
secret=$(git diff --cached -U0 | grep '^+' | grep -ci 'password *=' || true)
secret_file=$(git grep --cached -l -i 'password *=' | head -1 || true)
todos=$(git diff --cached -U0 | grep -c '^+.*TODO' || true)
removed=$(git diff --cached --numstat | awk '{d += $2} END {print d}')
echo "Fichiers modifies: $files"
if [ "$secret" -gt 0 ]; then echo "Secret detecte: oui ($secret_file)"; else echo "Secret detecte: non"; fi
echo "TODO ajoutes: $todos"
echo "Lignes supprimees: $removed (seuil 5)"
if [ "$secret" -gt 0 ] || [ "$removed" -gt 5 ]; then echo "Verdict: REFUSE"; else echo "Verdict: ACCEPTE"; fi
