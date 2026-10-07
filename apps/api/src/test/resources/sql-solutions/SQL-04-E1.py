import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE sales (region TEXT, month INTEGER, amount INTEGER);
    INSERT INTO sales VALUES
      ('Nord', 1, 100), ('Nord', 2, 150),
      ('Sud', 1, 80), ('Sud', 2, 100),
      ('Est', 1, 60);
    """
)

totals = (
    "SELECT region, SUM(amount) AS total FROM sales "
    "GROUP BY region HAVING SUM(amount) > 150 ORDER BY total DESC"
)
running = (
    "SELECT SUM(amount) OVER (ORDER BY month) FROM sales "
    "WHERE region = 'Nord' ORDER BY month"
)

print("Régions > 150:", ", ".join(f"{region}={total}" for region, total in connection.execute(totals)))
print("Cumul Nord:", ", ".join(str(row[0]) for row in connection.execute(running)))
