#!/usr/bin/env bash
mkdir -p cfg
cd cfg
cat > settings.json <<'EOF'
{"permissions":{"allow":["Bash(*)","Read(*)"],"deny":["Bash(rm -rf *)"]}}
EOF
audit() {
  local file="$1" count=0
  if jq -e '.permissions.allow | index("Bash(*)")' "$file" > /dev/null; then
    echo "Probleme: allow trop large (Bash(*))"; count=$((count + 1))
  fi
  if ! jq -e '.permissions.deny | index("Read(./.env)")' "$file" > /dev/null; then
    echo "Probleme: .env non protege"; count=$((count + 1))
  fi
  if ! jq -e '.permissions.deny | index("Bash(git push --force*)")' "$file" > /dev/null; then
    echo "Probleme: push force non interdit"; count=$((count + 1))
  fi
  echo "Problemes: $count"
}
audit settings.json
jq '.permissions.allow -= ["Bash(*)"] | .permissions.deny += ["Read(./.env)", "Bash(git push --force*)"]' settings.json > settings.fixed.json
audit settings.fixed.json
