import sqlite3

connection = sqlite3.connect(":memory:")
connection.executescript("""
create table evidence(id integer primary key, concept text not null, validated integer not null check(validated in (0, 1)));
insert into evidence(concept, validated) values ('SQL-TRANSACTIONS', 1), ('contrôle', 0);
""")
validated = connection.execute("select count(*) from evidence where validated = 1").fetchone()[0]
assert validated == 1
print("SQL-07: preuve validée")
connection.close()
