#!/usr/bin/env bash
mkdir -p reprise
cd reprise
printf 'echo app\n' > app.sh
echo '# notes' > notes.md
printf 'a,b\n1,2\n' > data.csv
echo 'TOKEN=sk-live-123' > .env
echo log > build.log
git init -q -b main
git config user.name "Eleve"
git config user.email "eleve@dlr.dev"
printf '.env\nbuild.log\n' > .gitignore
echo "Ignores: $(git check-ignore .env build.log | paste -sd' ')"
cat > .git/hooks/pre-commit <<'HOOK'
#!/usr/bin/env bash
if git diff --cached -U0 | grep -q '^+.*sk-live-'; then
  echo "secret detecte" >&2
  exit 1
fi
HOOK
chmod +x .git/hooks/pre-commit
git add app.sh
git commit -q -m "Ajoute le code"
git add notes.md
git commit -q -m "Ajoute la documentation"
git add data.csv
git commit -q -m "Ajoute les donnees"
printf 'test:\n\t@echo test ok\n' > Makefile
git add Makefile .gitignore
git commit -q -m "Ajoute les garde-fous"
echo 'cle=sk-live-999' > fuite.txt
git add fuite.txt
if git commit -q -m "Fuite" 2> /dev/null; then echo "Garde-fou: inactif"; else echo "Garde-fou: actif"; fi
git reset -q fuite.txt
rm fuite.txt
echo "Verification: $(make --no-print-directory test)"
echo "Commits: $(git rev-list --count HEAD)"
if [ -z "$(git status --porcelain)" ]; then echo "Statut Git: propre"; else echo "Statut Git: sale"; fi
