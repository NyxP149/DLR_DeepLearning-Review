import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute("CREATE TABLE user (id INTEGER PRIMARY KEY, email TEXT NOT NULL)")
connection.executemany(
    "INSERT INTO user (email) VALUES (?)",
    [(f"user{number}@dlr.dev",) for number in range(1000)],
)

TARGET = "user42@dlr.dev"


def plan(sql: str) -> str:
    rows = connection.execute("EXPLAIN QUERY PLAN " + sql, (TARGET,)).fetchall()
    detail = " ".join(row[3] for row in rows)
    return "recherche par index" if "SEARCH" in detail else "SCAN complet"


exact = "SELECT id FROM user WHERE email = ?"
print("Avant index:", plan(exact))

connection.execute("CREATE INDEX idx_user_email ON user(email)")
print("Après index:", plan(exact))

lowered = "SELECT id FROM user WHERE lower(email) = ?"
print("Avec lower(email):", plan(lowered))

found = connection.execute(exact, (TARGET,)).fetchall()
print("Lignes trouvées:", len(found))
