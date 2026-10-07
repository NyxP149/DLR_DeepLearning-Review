import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute("CREATE TABLE product (name TEXT, category TEXT, price INTEGER)")
connection.executemany(
    "INSERT INTO product VALUES (?, ?, ?)",
    [
        ("Souris", "peripherique", 20),
        ("Clavier", "peripherique", 50),
        ("Câble", "peripherique", 10),
        ("Écran", "peripherique", 200),
        ("Webcam", "peripherique", 80),
        ("Chaise", "mobilier", 120),
    ],
)

PAGE_SIZE = 2


def page(number: int) -> list[str]:
    sql = (
        "SELECT name FROM product "
        "WHERE category = 'peripherique' AND price >= 20 "
        "ORDER BY price DESC, name "
        "LIMIT ? OFFSET ?"
    )
    rows = connection.execute(sql, (PAGE_SIZE, (number - 1) * PAGE_SIZE)).fetchall()
    return [row[0] for row in rows]


for number in (1, 2, 3):
    names = page(number)
    print(f"Page {number}: {', '.join(names) if names else '(vide)'}")
