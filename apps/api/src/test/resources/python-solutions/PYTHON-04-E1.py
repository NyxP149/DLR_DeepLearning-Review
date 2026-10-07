import json
from pathlib import Path

path = Path("profile.json")
profile = {"name": "Nyx", "level": 3}
path.write_text(json.dumps(profile), encoding="utf-8")

with path.open(encoding="utf-8") as stream:
    loaded = json.load(stream)

print(f"Profil: {loaded['name']} | niveau {loaded['level']}")
