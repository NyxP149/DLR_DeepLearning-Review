import os
import sqlite3

if os.path.exists("bank.db"):
    os.remove("bank.db")

writer = sqlite3.connect("bank.db", isolation_level=None)
reader = sqlite3.connect("bank.db", isolation_level=None)
writer.execute("CREATE TABLE account (id TEXT PRIMARY KEY, balance INTEGER NOT NULL CHECK (balance >= 0))")
writer.executemany("INSERT INTO account VALUES (?, ?)", [("A", 100), ("B", 0)])


def state(connection: sqlite3.Connection) -> str:
    rows = connection.execute("SELECT id, balance FROM account ORDER BY id").fetchall()
    return ", ".join(f"{account}={balance}" for account, balance in rows)


def transfer(amount: int) -> str:
    writer.execute("BEGIN")
    try:
        writer.execute("UPDATE account SET balance = balance + ? WHERE id = 'B'", (amount,))
        writer.execute("UPDATE account SET balance = balance - ? WHERE id = 'A'", (amount,))
        writer.execute("COMMIT")
        return "OK"
    except sqlite3.IntegrityError:
        writer.execute("ROLLBACK")
        return "refusé"


print(f"Virement 30: {transfer(30)} -> {state(writer)}")
print(f"Virement 500: {transfer(500)} -> {state(writer)}")

writer.execute("BEGIN")
writer.execute("UPDATE account SET balance = balance - 10 WHERE id = 'A'")
print(f"Écrivain voit: {state(writer)}")
print(f"Lecteur voit: {state(reader)}")
writer.execute("ROLLBACK")
print(f"Après annulation: {state(writer)}")

writer.close()
reader.close()
