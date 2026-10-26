import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE author (id INTEGER PRIMARY KEY, name TEXT);
    CREATE TABLE article (id INTEGER PRIMARY KEY, author_id INTEGER, title TEXT);
    """
)
connection.executemany("INSERT INTO author (name) VALUES (?)", [(f"auteur-{i}",) for i in range(50)])
connection.executemany(
    "INSERT INTO article (author_id, title) VALUES (?, ?)",
    [(i + 1, f"t{i}") for i in range(50) if i % 3 != 0],
)

queries = 0


def run(sql, params=()):
    global queries
    queries += 1
    return connection.execute(sql, params).fetchall()


authors = run("SELECT id, name FROM author ORDER BY id")
n_plus_one = {name: run("SELECT COUNT(*) FROM article WHERE author_id = ?", (author_id,))[0][0] for author_id, name in authors}
print(f"N+1: {queries} requetes")
queries = 0
joined = dict(run(
    "SELECT a.name, COUNT(r.id) FROM author a LEFT JOIN article r ON r.author_id = a.id GROUP BY a.id ORDER BY a.id"
))
print(f"JOIN: {queries} requete")
print("Resultats identiques:", n_plus_one == joined)


def plan():
    detail = " ".join(row[3] for row in connection.execute(
        "EXPLAIN QUERY PLAN SELECT COUNT(*) FROM article WHERE author_id = 7"))
    return "recherche par index" if "SEARCH" in detail else "SCAN complet"


print("Plan sans index:", plan())
connection.execute("CREATE INDEX idx_article_author ON article(author_id)")
print("Plan avec index:", plan())
