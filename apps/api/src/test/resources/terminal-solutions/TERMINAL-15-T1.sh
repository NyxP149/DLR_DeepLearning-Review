#!/usr/bin/env bash
mkdir -p depot
cd depot
git init -q -b main
git config user.name "Eleve"
git config user.email "eleve@dlr.dev"
echo v1 > readme.txt
git add readme.txt
git commit -q -m "Ajoute readme"
echo v2 > readme.txt
echo todo > todo.txt
echo "Statut:"
git status --short
git add readme.txt todo.txt
git commit -q -m "Met a jour readme et ajoute todo"
echo "Historique:"
git log --format=%s
echo "Fichiers suivis: $(git ls-files | tr '\n' ' ' | sed 's/ $//')"
