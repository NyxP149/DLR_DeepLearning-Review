import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE customer (id INTEGER PRIMARY KEY, name TEXT);
    CREATE TABLE orders (id INTEGER PRIMARY KEY, customer_id INTEGER, amount INTEGER);
    """
)
connection.executemany("INSERT INTO customer VALUES (?, ?)", [(i, f"client{i}") for i in range(1, 101)])
connection.executemany(
    "INSERT INTO orders (customer_id, amount) VALUES (?, ?)",
    [((i % 100) + 1, 10 + i % 7) for i in range(300)],
)

statements: list[str] = []
connection.set_trace_callback(lambda sql: statements.append(sql) if sql.lstrip().upper().startswith("SELECT") else None)


def n_plus_one() -> dict[int, int]:
    totals = {}
    for (customer_id,) in connection.execute("SELECT id FROM customer ORDER BY id").fetchall():
        row = connection.execute("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE customer_id = ?", (customer_id,)).fetchone()
        totals[customer_id] = row[0]
    return totals


def plural(count: int) -> str:
    return f"{count} requête" + ("s" if count > 1 else "")


def plan() -> str:
    rows = connection.execute("EXPLAIN QUERY PLAN SELECT SUM(amount) FROM orders WHERE customer_id = ?", (7,)).fetchall()
    return "recherche par index" if "SEARCH" in " ".join(row[3] for row in rows) else "SCAN complet"


slow = n_plus_one()
print(f"N+1: {plural(len(statements))}")

JOIN_SQL = (
    "SELECT c.id, COALESCE(SUM(o.amount), 0) FROM customer c "
    "LEFT JOIN orders o ON o.customer_id = c.id GROUP BY c.id ORDER BY c.id"
)
statements.clear()
fast = dict(connection.execute(JOIN_SQL).fetchall())
print(f"JOIN: {plural(len(statements))}")
print(f"Résultats identiques: {fast == slow}")

print(f"Plan sans index: {plan()}")
connection.execute("CREATE INDEX idx_orders_customer ON orders(customer_id)")
print(f"Plan avec index: {plan()}")
