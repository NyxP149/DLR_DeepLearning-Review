import sqlite3


class UserRepository:
    def __init__(self, connection: sqlite3.Connection) -> None:
        self.connection = connection
        connection.execute(
            "CREATE TABLE IF NOT EXISTS user (id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE)"
        )

    def add_many(self, users: list[tuple[str, str]]) -> int:
        try:
            with self.connection:
                self.connection.executemany("INSERT INTO user (name, email) VALUES (?, ?)", users)
        except sqlite3.IntegrityError as error:
            raise ValueError("email déjà utilisé") from error
        return len(users)

    def count(self) -> int:
        return self.connection.execute("SELECT COUNT(*) FROM user").fetchone()[0]

    def find_by_email(self, email: str) -> tuple[str, str] | None:
        row = self.connection.execute("SELECT name, email FROM user WHERE email = ?", (email,)).fetchone()
        return (row[0], row[1]) if row else None


repository = UserRepository(sqlite3.connect(":memory:"))

print(f"Lot 1: {repository.add_many([('Ada', 'ada@dlr.dev'), ('Linus', 'linus@dlr.dev')])} ajouté(s)")
try:
    repository.add_many([("Grace", "grace@dlr.dev"), ("Ada bis", "ada@dlr.dev")])
except ValueError as error:
    print(f"Lot 2 annulé: {error}")

print(f"Total en base: {repository.count()}")
found = repository.find_by_email("ada@dlr.dev")
print("Trouvé:", f"{found[0]} <{found[1]}>" if found else "aucun")
