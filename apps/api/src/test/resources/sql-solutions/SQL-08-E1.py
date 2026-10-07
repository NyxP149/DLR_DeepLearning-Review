import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript(
    """
    CREATE TABLE employee (id INTEGER PRIMARY KEY, name TEXT, manager_id INTEGER, salary INTEGER);
    INSERT INTO employee VALUES
      (1, 'Ada', NULL, 5000),
      (2, 'Linus', 1, 4000),
      (3, 'Grace', 2, 3000),
      (4, 'Alan', 1, 2000);
    """
)

chain = """
WITH RECURSIVE chain(id, name, manager_id, depth) AS (
  SELECT id, name, manager_id, 0 FROM employee WHERE name = 'Grace'
  UNION ALL
  SELECT e.id, e.name, e.manager_id, chain.depth + 1
  FROM employee e JOIN chain ON e.id = chain.manager_id
)
SELECT name FROM chain ORDER BY depth
"""
rich = """
WITH average AS (SELECT AVG(salary) AS value FROM employee)
SELECT name FROM employee, average WHERE salary > average.value ORDER BY salary DESC
"""

print("Chaîne de Grace:", " > ".join(row[0] for row in connection.execute(chain)))
print("Au-dessus de la moyenne:", ", ".join(row[0] for row in connection.execute(rich)))
