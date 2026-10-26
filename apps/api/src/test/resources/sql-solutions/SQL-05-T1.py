import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute(
    "CREATE TABLE employee (email TEXT UNIQUE, name TEXT NOT NULL, salary INTEGER CHECK (salary >= 0))"
)
rows = [
    ("ada@dlr.dev", "Ada", 3000),
    ("ada@dlr.dev", "Ada bis", 3100),
    ("nyx@dlr.dev", None, 2800),
    ("max@dlr.dev", "Max", -10),
]
for row in rows:
    try:
        connection.execute("INSERT INTO employee VALUES (?, ?, ?)", row)
        print(f"{row}: accepte")
    except sqlite3.IntegrityError as error:
        message = str(error)
        kind = "UNIQUE" if "UNIQUE" in message else "NOT NULL" if "NOT NULL" in message else "CHECK"
        print(f"{row}: refuse ({kind})")
print("Lignes:", connection.execute("SELECT COUNT(*) FROM employee").fetchone()[0])
