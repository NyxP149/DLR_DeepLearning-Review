import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE sale (month TEXT, product TEXT, amount INTEGER);
    INSERT INTO sale VALUES
      ('2024-01', 'stylo', 40), ('2024-02', 'stylo', 70), ('2024-03', 'stylo', 30),
      ('2024-01', 'cahier', 60), ('2024-02', 'cahier', 25), ('2024-01', 'gomme', 15);
    """
)
totals = (
    "SELECT product, SUM(amount) AS total FROM sale GROUP BY product HAVING SUM(amount) > 80 ORDER BY total DESC"
)
running = (
    "SELECT month, SUM(amount) OVER (ORDER BY month) FROM sale WHERE product = 'stylo' ORDER BY month"
)
print("Produits > 80:", ", ".join(f"{name}={total}" for name, total in connection.execute(totals)))
print("Cumul stylo:", ", ".join(str(row[1]) for row in connection.execute(running)))
