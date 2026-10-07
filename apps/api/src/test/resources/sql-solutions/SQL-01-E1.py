import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute("PRAGMA foreign_keys = ON")

connection.execute("CREATE TABLE customer (id INTEGER PRIMARY KEY, email TEXT NOT NULL UNIQUE)")
connection.execute(
    "CREATE TABLE orders ("
    "id INTEGER PRIMARY KEY, "
    "customer_id INTEGER NOT NULL REFERENCES customer(id), "
    "total_cents INTEGER NOT NULL)"
)

for table in ("customer", "orders"):
    columns = [row[1] for row in connection.execute(f"PRAGMA table_info({table})")]
    print(f"{table}: {', '.join(columns)}")

for row in connection.execute("PRAGMA foreign_key_list(orders)"):
    print(f"Clé étrangère: orders.{row[3]} -> {row[2]}.{row[4]}")

try:
    connection.execute("INSERT INTO orders (customer_id, total_cents) VALUES (99, 1000)")
except sqlite3.Error as error:
    print(f"Insertion orpheline refusée: {error}")
