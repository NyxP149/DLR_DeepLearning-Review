import os
import sqlite3

if os.path.exists("stock.db"):
    os.remove("stock.db")

writer = sqlite3.connect("stock.db", isolation_level=None)
writer.execute("CREATE TABLE stock (item TEXT PRIMARY KEY, quantity INTEGER CHECK (quantity >= 0))")
writer.execute("INSERT INTO stock VALUES ('pomme', 10)")


def quantity(connection):
    return connection.execute("SELECT quantity FROM stock WHERE item = 'pomme'").fetchone()[0]


def reserve(amount):
    writer.execute("BEGIN")
    try:
        writer.execute("UPDATE stock SET quantity = quantity - ? WHERE item = 'pomme'", (amount,))
        writer.execute("COMMIT")
        return "OK"
    except sqlite3.IntegrityError:
        writer.execute("ROLLBACK")
        return "refusee"


print(f"Reservation 3: {reserve(3)} -> pomme={quantity(writer)}")
print(f"Reservation 20: {reserve(20)} -> pomme={quantity(writer)}")
reader = sqlite3.connect("stock.db", isolation_level=None)
writer.execute("BEGIN")
writer.execute("UPDATE stock SET quantity = quantity - 2 WHERE item = 'pomme'")
print(f"Ecrivain voit: pomme={quantity(writer)}")
print(f"Lecteur voit: pomme={quantity(reader)}")
writer.execute("ROLLBACK")
print(f"Apres annulation: pomme={quantity(writer)}")
