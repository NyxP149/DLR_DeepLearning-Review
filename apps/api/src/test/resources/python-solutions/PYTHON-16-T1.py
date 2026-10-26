import sqlite3


class ProductRepository:
    def __init__(self):
        self.connection = sqlite3.connect(":memory:")
        self.connection.execute("CREATE TABLE products (sku TEXT UNIQUE NOT NULL, name TEXT NOT NULL, price REAL NOT NULL)")

    def add_many(self, products):
        try:
            with self.connection:
                self.connection.executemany("INSERT INTO products (sku, name, price) VALUES (?, ?, ?)", products)
        except sqlite3.IntegrityError:
            raise ValueError("sku deja utilise") from None
        return len(products)

    def count(self):
        return self.connection.execute("SELECT COUNT(*) FROM products").fetchone()[0]

    def find(self, sku):
        return self.connection.execute("SELECT sku, name, price FROM products WHERE sku = ?", (sku,)).fetchone()


repository = ProductRepository()
print(f"Lot 1: {repository.add_many([('K-1', 'Clavier', 49.9), ('S-2', 'Souris', 19.9)])} ajoute(s)")
try:
    repository.add_many([("C-3", "Cable", 4.5), ("K-1", "Clavier bis", 39.9)])
except ValueError as error:
    print(f"Lot 2 annule: {error}")
print(f"Total en base: {repository.count()}")
sku, name, price = repository.find("K-1")
print(f"Trouve: {sku} {name} {price}")
