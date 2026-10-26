import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute("PRAGMA foreign_keys = ON")
connection.executescript(
    """
    CREATE TABLE author (id INTEGER PRIMARY KEY, name TEXT NOT NULL);
    CREATE TABLE book (
        id INTEGER PRIMARY KEY,
        author_id INTEGER NOT NULL REFERENCES author(id),
        title TEXT NOT NULL,
        isbn TEXT UNIQUE
    );
    """
)
for table in ("author", "book"):
    columns = [row[1] for row in connection.execute(f"PRAGMA table_info({table})")]
    print(f"{table}: {', '.join(columns)}")
for row in connection.execute("PRAGMA foreign_key_list(book)"):
    print(f"Cle etrangere: book.{row[3]} -> {row[2]}.{row[4]}")
try:
    connection.execute("INSERT INTO book (author_id, title, isbn) VALUES (99, 'Orphelin', 'X-1')")
except sqlite3.IntegrityError as error:
    print(f"Insertion orpheline refusee: {error}")
