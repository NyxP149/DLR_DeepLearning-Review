import sqlite3
from pathlib import Path

BASE = Path("reports").resolve()


def safe_path(user_path: str) -> Path:
    candidate = (BASE / user_path).resolve()
    if not candidate.is_relative_to(BASE):
        raise ValueError("sortie du dossier")
    return candidate


def find_user(connection: sqlite3.Connection, name: str) -> list:
    return connection.execute("SELECT id FROM user WHERE name = ?", (name,)).fetchall()


def mask(secret: str) -> str:
    return "****" + secret[-4:]


for user_path in ("../../etc/passwd", "q1.csv"):
    try:
        safe_path(user_path)
        print(f"{user_path}: accepté")
    except ValueError as error:
        print(f"{user_path}: refusé ({error})")

connection = sqlite3.connect(":memory:")
connection.execute("CREATE TABLE user (id INTEGER PRIMARY KEY, name TEXT)")
connection.execute("INSERT INTO user (name) VALUES ('Ada')")
payload = "x' OR '1'='1"
unsafe = connection.execute("SELECT id FROM user WHERE name = '" + payload + "'").fetchall()
print(f"Concaténation (à ne jamais faire): {len(unsafe)} ligne(s)")
print(f"Requête paramétrée: {len(find_user(connection, payload))} ligne(s)")
print(f"Secret masqué: {mask('sk-live-abcdef')}")
