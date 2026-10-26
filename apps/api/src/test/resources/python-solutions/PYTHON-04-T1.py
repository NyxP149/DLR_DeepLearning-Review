import json
from pathlib import Path

path = Path("settings.json")
log_path = Path("journal.txt")
path.write_text(json.dumps({"thème": "sombre", "taille": 14}, ensure_ascii=False), encoding="utf-8")
with path.open(encoding="utf-8") as handle:
    settings = json.load(handle)
print(f"Reglages: theme={settings['thème']} taille={settings['taille']}")
print("Fichier ferme:", handle.closed)

partial = json.loads('{"taille": 12}')
try:
    partial["langue"]
except KeyError as error:
    print("Champ manquant:", error)
print("Accents conserves:", "thème" in path.read_text(encoding="utf-8"))

with log_path.open("w", encoding="utf-8") as journal:
    for line in ("demarrage", "lecture", "arret"):
        journal.write(line + chr(10))
print("Lignes ecrites:", len(log_path.read_text(encoding="utf-8").splitlines()))
