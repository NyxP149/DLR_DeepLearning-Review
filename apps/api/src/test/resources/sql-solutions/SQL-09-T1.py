import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE product (id INTEGER PRIMARY KEY, name TEXT, category TEXT, unit_price INTEGER);
    CREATE TABLE store (id INTEGER PRIMARY KEY, region TEXT);
    CREATE TABLE sale (product_id INTEGER, store_id INTEGER, quantity INTEGER);
    INSERT INTO product VALUES (1, 'Stylo', 'Papeterie', 2), (2, 'Bureau', 'Mobilier', 150);
    INSERT INTO store VALUES (1, 'Nord'), (2, 'Sud');
    INSERT INTO sale VALUES (1, 1, 100), (2, 1, 1), (1, 2, 50), (2, 2, 2), (2, 1, 1);
    CREATE VIEW region_revenue AS
    SELECT st.region AS region, p.category AS category, SUM(s.quantity * p.unit_price) AS revenue
    FROM sale s JOIN product p ON p.id = s.product_id JOIN store st ON st.id = s.store_id
    GROUP BY st.region, p.category;
    """
)
for region, category, revenue in connection.execute("SELECT * FROM region_revenue ORDER BY region, category"):
    print(f"{region} | {category} | {revenue}")
print("Total:", connection.execute("SELECT SUM(revenue) FROM region_revenue").fetchone()[0])
