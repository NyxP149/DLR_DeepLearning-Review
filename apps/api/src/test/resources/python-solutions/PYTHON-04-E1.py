import json
from pathlib import Path

path = Path("profile.json")
profile = {"name": "Nyx", "level": 3, "motto": "Révision"}
log_path = Path("journal.txt")

with path.open("w", encoding="utf-8") as handle:
    json.dump(profile, handle, ensure_ascii=False)

with path.open(encoding="utf-8") as handle:
    data = json.load(handle)

print(f"Profil: {data['name']} | niveau {data['level']}")
print(f"Fichier ferme: {handle.closed}")

incomplete = json.loads('{"name": "Ada"}')
for field in ("name", "level"):
    if field not in incomplete:
        print(f"Champ manquant: {field!r}")

print(f"Accents conserves: {data['motto'] == 'Révision'}")

with log_path.open("w", encoding="utf-8") as handle:
    for line in ("a", "b", "c"):
        handle.write(line + "\n")
print(f"Lignes ecrites: {len(log_path.read_text(encoding='utf-8').splitlines())}")
