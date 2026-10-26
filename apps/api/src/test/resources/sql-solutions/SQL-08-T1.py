import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE category (id INTEGER PRIMARY KEY, name TEXT, parent_id INTEGER);
    INSERT INTO category VALUES (1, 'Electronique', NULL), (2, 'Audio', 1), (3, 'Casques', 2);
    CREATE TABLE product (name TEXT, price INTEGER);
    INSERT INTO product VALUES ('Casque pro', 250), ('Casque eco', 40), ('Micro', 120), ('Cable', 10);
    """
)
chain = """
WITH RECURSIVE ancestors(id, name, parent_id, depth) AS (
    SELECT id, name, parent_id, 0 FROM category WHERE name = 'Casques'
    UNION ALL
    SELECT c.id, c.name, c.parent_id, a.depth + 1 FROM category c JOIN ancestors a ON c.id = a.parent_id
)
SELECT name FROM ancestors ORDER BY depth
"""
above = """
WITH stats AS (SELECT AVG(price) AS average FROM product)
SELECT name FROM product, stats WHERE price > average ORDER BY price DESC
"""
print("Chaine de Casques:", " > ".join(row[0] for row in connection.execute(chain)))
print("Au-dessus de la moyenne:", ", ".join(row[0] for row in connection.execute(above)))
