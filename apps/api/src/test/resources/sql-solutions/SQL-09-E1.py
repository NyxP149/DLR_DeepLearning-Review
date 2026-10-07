import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE dim_date (id INTEGER PRIMARY KEY, month TEXT);
    CREATE TABLE dim_product (id INTEGER PRIMARY KEY, name TEXT, category TEXT);
    CREATE TABLE fact_sales (date_id INTEGER, product_id INTEGER, amount INTEGER);
    INSERT INTO dim_date VALUES (1, '2024-01'), (2, '2024-02');
    INSERT INTO dim_product VALUES (1, 'Clavier', 'Périphériques'), (2, 'Écran', 'Périphériques'), (3, 'Chaise', 'Mobilier');
    INSERT INTO fact_sales VALUES (1, 1, 100), (1, 2, 150), (1, 3, 120), (2, 1, 80);
    """
)

connection.execute(
    """
    CREATE VIEW monthly_revenue AS
    SELECT d.month AS month, p.category AS category, SUM(f.amount) AS revenue
    FROM fact_sales f
    JOIN dim_date d ON d.id = f.date_id
    JOIN dim_product p ON p.id = f.product_id
    GROUP BY d.month, p.category
    """
)

for month, category, revenue in connection.execute(
    "SELECT month, category, revenue FROM monthly_revenue ORDER BY month, category"
):
    print(f"{month} | {category} | {revenue}")

total = connection.execute("SELECT SUM(revenue) FROM monthly_revenue").fetchone()[0]
print(f"Total: {total}")
