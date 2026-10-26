#!/usr/bin/env bash
mkdir -p depot
cd depot
git init -q -b main
git config user.name "Eleve"
git config user.email "eleve@dlr.dev"
echo "level=info" > app.conf
git add app.conf
git commit -q -m "Initial"
git checkout -q -b feature
echo "level=debug" > app.conf
git commit -q -am "Passe en debug"
git checkout -q main
echo "level=warn" > app.conf
git commit -q -am "Passe en warn"
git merge feature > /dev/null 2>&1 || echo "Conflit: $(git diff --name-only --diff-filter=U)"
echo "level=warn" > app.conf
git add app.conf
git commit -q -m "Fusionne feature"
echo "Resolu: $(cat app.conf)"
echo "Historique: $(git log --first-parent --format=%s | paste -sd,)"
echo "Branches: $(git branch --format='%(refname:short)' | paste -sd' ')"
echo "build.log" > .gitignore
touch build.log
echo "Ignore: $(git check-ignore build.log)"
