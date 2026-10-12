#!/usr/bin/env bash
git config --global user.name "Ada"
git config --global user.email "ada@dlr.dev"
git config --global init.defaultBranch main
mkdir projet && cd projet
git init -q
echo "v1" > app.txt

git add app.txt
git commit -q -m "Ajoute app"
echo "v2" > app.txt
echo "notes" > notes.txt
echo "Statut:"
git status --short
echo "Diff: $(git diff --numstat | awk '{print "+"$1" -"$2" "$3}')"
git add -A
git commit -q -m "Met à jour app et ajoute notes"
echo "Historique:"
git log --format=%s
echo "Fichiers suivis: $(git ls-files | xargs)"
