import sqlite3

connection = sqlite3.connect(":memory:")

connection.execute(
    "CREATE TABLE product ("
    "sku TEXT NOT NULL UNIQUE, "
    "name TEXT NOT NULL, "
    "price INTEGER NOT NULL CHECK (price > 0))"
)

attempts = [
    ("A1", "Clavier", 50),
    ("A1", "Souris", 20),
    ("B2", None, 10),
    ("C3", "Câble", -5),
]

for attempt in attempts:
    try:
        connection.execute("INSERT INTO product VALUES (?, ?, ?)", attempt)
        print(f"{attempt}: accepté")
    except sqlite3.Error as error:
        print(f"{attempt}: refusé ({str(error).split(' constraint failed')[0]})")

count = connection.execute("SELECT COUNT(*) FROM product").fetchone()[0]
print(f"Lignes: {count}")
