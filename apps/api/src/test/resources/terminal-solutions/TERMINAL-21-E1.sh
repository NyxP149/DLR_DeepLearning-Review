#!/usr/bin/env bash
mkdir -p .claude
cat > .claude/settings.json <<'JSON'
{
  "permissions": {
    "allow": ["Bash(*)", "Read", "Bash(npm test:*)"],
    "deny": ["Bash(rm -rf:*)"]
  }
}
JSON

audit() {
  local file=$1 n=0
  if jq -e '.permissions.allow | index("Bash(*)")' "$file" >/dev/null; then
    echo "Problème: allow trop large (Bash(*))"
    n=$((n + 1))
  fi
  if ! jq -e '.permissions.deny | index("Read(./.env)")' "$file" >/dev/null; then
    echo "Problème: .env non protégé"
    n=$((n + 1))
  fi
  if ! jq -e '.permissions.deny | index("Bash(git push:*)")' "$file" >/dev/null; then
    echo "Problème: git push non interdit"
    n=$((n + 1))
  fi
  echo "Problèmes: $n"
}

audit .claude/settings.json

jq '.permissions.allow -= ["Bash(*)"] | .permissions.deny += ["Read(./.env)", "Bash(git push:*)"]' \
  .claude/settings.json > .claude/settings.fixed.json
[ -f .claude/settings.fixed.json ] && audit .claude/settings.fixed.json
true
