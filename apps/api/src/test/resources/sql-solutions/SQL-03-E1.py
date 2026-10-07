import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE customer (id INTEGER PRIMARY KEY, name TEXT);
    CREATE TABLE orders (id INTEGER PRIMARY KEY, customer_id INTEGER, amount INTEGER);
    INSERT INTO customer VALUES (1, 'Ada'), (2, 'Linus'), (3, 'Grace');
    INSERT INTO orders VALUES (1, 1, 100), (2, 1, 50), (3, 2, 70);
    """
)

ordered = (
    "SELECT DISTINCT c.name FROM customer c "
    "INNER JOIN orders o ON o.customer_id = c.id ORDER BY c.name"
)
counts = (
    "SELECT c.name, COUNT(o.id) FROM customer c "
    "LEFT JOIN orders o ON o.customer_id = c.id GROUP BY c.id ORDER BY c.name"
)
without = (
    "SELECT c.name FROM customer c "
    "LEFT JOIN orders o ON o.customer_id = c.id WHERE o.id IS NULL ORDER BY c.name"
)

print("Ont commandé:", ", ".join(row[0] for row in connection.execute(ordered)))
print("Commandes:", ", ".join(f"{name}={total}" for name, total in connection.execute(counts)))
print("Sans commande:", ", ".join(row[0] for row in connection.execute(without)))
