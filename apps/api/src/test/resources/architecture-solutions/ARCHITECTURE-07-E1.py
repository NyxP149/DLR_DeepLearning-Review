rows = []
def post(name: str) -> dict:
    item = {"id": len(rows) + 1, "name": name.strip()}
    rows.append(item)
    return item
created = post("  Architecture DLR  ")
print(f"POST /items -> 201 #{created['id']}")
print("Vue:", rows[0]["name"])
