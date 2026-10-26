#!/usr/bin/env bash
mkdir -p depot
cd depot
git init -q -b main
git config user.name "Eleve"
git config user.email "eleve@dlr.dev"
cat > .git/hooks/pre-commit <<'HOOK'
#!/usr/bin/env bash
if git diff --cached --name-only | grep -q '\.pem$'; then
  echo "fichier .pem interdit" >&2
  exit 1
fi
if git diff --cached -U0 | grep -q '^+.*BEGIN PRIVATE KEY'; then
  echo "cle privee detectee" >&2
  exit 1
fi
HOOK
chmod +x .git/hooks/pre-commit

attempt() {
  local message="$1" out rc
  set +e
  out=$(git commit -q -m "$message" 2>&1)
  rc=$?
  set -e
  if [ "$rc" -ne 0 ]; then
    echo "Commit bloque: code $rc"
    echo "Message du hook: $(echo "$out" | head -1)"
  else
    echo "Commit accepte"
  fi
}

echo x > server.pem
git add server.pem
attempt "Ajoute un certificat"
git reset -q server.pem
rm server.pem
printf -- '-----BEGIN PRIVATE KEY-----\n' > notes.txt
git add notes.txt
attempt "Ajoute des notes"
git reset -q notes.txt
rm notes.txt
echo "# Projet" > README.md
git add README.md
attempt "Ajoute le readme"
echo "Historique: $(git log --format=%s | paste -sd,)"
