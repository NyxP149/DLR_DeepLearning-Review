import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute("CREATE TABLE orders (id INTEGER PRIMARY KEY, reference TEXT, total INTEGER)")
connection.executemany(
    "INSERT INTO orders (reference, total) VALUES (?, ?)",
    [(f"ref-{i}", i) for i in range(2000)],
)


def plan(sql, params):
    detail = " ".join(row[3] for row in connection.execute("EXPLAIN QUERY PLAN " + sql, params))
    return "recherche par index" if "SEARCH" in detail and "INDEX" in detail else "SCAN complet"


EXACT = "SELECT * FROM orders WHERE reference = ?"
WRAPPED = "SELECT * FROM orders WHERE upper(reference) = ?"
print("Avant index:", plan(EXACT, ("ref-1500",)))
connection.execute("CREATE INDEX idx_orders_reference ON orders(reference)")
print("Apres index:", plan(EXACT, ("ref-1500",)))
print("Avec upper(reference):", plan(WRAPPED, ("REF-1500",)))
print("Lignes trouvees:", len(connection.execute(EXACT, ("ref-1500",)).fetchall()))
