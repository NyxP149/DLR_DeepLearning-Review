import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE student (id INTEGER PRIMARY KEY, name TEXT);
    CREATE TABLE enrollment (id INTEGER PRIMARY KEY, student_id INTEGER, course TEXT);
    INSERT INTO student VALUES (1, 'Zoe'), (2, 'Max'), (3, 'Eve');
    INSERT INTO enrollment VALUES (1, 1, 'SQL'), (2, 1, 'Java'), (3, 3, 'SQL');
    """
)
enrolled = (
    "SELECT DISTINCT s.name FROM student s INNER JOIN enrollment e ON e.student_id = s.id ORDER BY s.name"
)
counts = (
    "SELECT s.name, COUNT(e.id) FROM student s LEFT JOIN enrollment e ON e.student_id = s.id "
    "GROUP BY s.id ORDER BY s.name"
)
without = (
    "SELECT s.name FROM student s LEFT JOIN enrollment e ON e.student_id = s.id WHERE e.id IS NULL ORDER BY s.name"
)
print("Inscrits:", ", ".join(row[0] for row in connection.execute(enrolled)))
print("Inscriptions:", ", ".join(f"{name}={total}" for name, total in connection.execute(counts)))
print("Sans inscription:", ", ".join(row[0] for row in connection.execute(without)))
