#!/usr/bin/env bash
git config --global user.name "Ada"
git config --global user.email "ada@dlr.dev"
git config --global init.defaultBranch main
mkdir projet && cd projet
git init -q
echo "mode=dev" > config.txt
git add config.txt && git commit -q -m "Initial"

git switch -q -c feature
echo "mode=prod" > config.txt
git commit -q -am "Passe en prod"

git switch -q main
echo "mode=test" > config.txt
git commit -q -am "Passe en test"

git merge feature >/dev/null 2>&1 || true
echo "Conflit: $(git diff --name-only --diff-filter=U)"
echo "mode=prod" > config.txt
git add config.txt
git commit -q -m "Fusionne feature"
echo "Résolu: $(cat config.txt)"
echo "Historique: $(git log --first-parent --format=%s | paste -sd, -)"
echo "Branches: $(git branch --format='%(refname:short)' | xargs)"
echo '*.log' > .gitignore
touch build.log
echo "Ignoré: $(git check-ignore build.log)"
