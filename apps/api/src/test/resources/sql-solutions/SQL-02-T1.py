import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE book (id INTEGER PRIMARY KEY, title TEXT, genre TEXT, pages INTEGER);
    INSERT INTO book (title, genre, pages) VALUES
      ('Nuit blanche', 'policier', 320), ('Le Pont', 'policier', 210), ('Recettes', 'cuisine', 400),
      ('Brume', 'policier', 210), ('Court', 'policier', 90), ('Dernier train', 'policier', 280);
    """
)
QUERY = (
    "SELECT title FROM book WHERE genre = ? AND pages >= ? "
    "ORDER BY pages DESC, title LIMIT ? OFFSET ?"
)
for page in (1, 2, 3):
    titles = [row[0] for row in connection.execute(QUERY, ("policier", 200, 2, (page - 1) * 2))]
    print(f"Page {page}: {', '.join(titles) if titles else '(vide)'}")
