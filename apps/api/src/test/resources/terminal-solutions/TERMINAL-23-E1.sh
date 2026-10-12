#!/usr/bin/env bash
git config --global user.name "Ada"
git config --global user.email "ada@dlr.dev"
git config --global init.defaultBranch main
mkdir projet && cd projet
git init -q

cat > .git/hooks/pre-commit <<'HOOK'
#!/usr/bin/env bash
if git diff --cached --name-only | grep -qx '.env'; then
  echo "fichier .env interdit" >&2
  exit 1
fi
if git diff --cached -U0 | grep -q '^+.*sk-live-'; then
  echo "secret détecté" >&2
  exit 1
fi
HOOK
chmod +x .git/hooks/pre-commit

echo 'KEY = "sk-live-abc"' > config.py
git add config.py
git commit -q -m "Ajoute la configuration" 2>hook.err
echo "Commit bloqué: code $?"
echo "Message du hook: $(cat hook.err)"

echo 'KEY = os.environ["KEY"]' > config.py
echo 'SECRET=x' > .env
git add config.py && git add -f .env
git commit -q -m "Ajoute la configuration" 2>hook.err
echo "Commit bloqué: code $?"
echo "Message du hook: $(cat hook.err)"

git rm --cached -q .env
git commit -q -m "Ajoute la configuration" 2>hook.err && echo "Commit accepté"
echo "Historique: $(git log --format=%s | paste -sd, -)"
