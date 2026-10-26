rows: dict[int, dict] = {}
next_id = 1


def post(name: str) -> tuple[int, dict]:
    global next_id
    cleaned = name.strip()
    if not cleaned:
        return 400, {"error": "nom vide"}
    if any(item["name"].lower() == cleaned.lower() for item in rows.values()):
        return 409, {"error": "doublon"}
    item = {"id": next_id, "name": cleaned}
    rows[next_id] = item
    next_id += 1
    return 201, item


def get(item_id: int) -> tuple[int, dict]:
    if item_id in rows:
        return 200, rows[item_id]
    return 404, {"error": "introuvable"}


def put(item_id: int, name: str) -> tuple[int, dict]:
    if item_id not in rows:
        return 404, {"error": "introuvable"}
    cleaned = name.strip()
    if not cleaned:
        return 400, {"error": "nom vide"}
    rows[item_id]["name"] = cleaned
    return 200, rows[item_id]


def delete(item_id: int) -> tuple[int, dict]:
    if rows.pop(item_id, None) is not None:
        return 204, {}
    return 404, {"error": "introuvable"}


def list_items() -> tuple[int, list]:
    return 200, [rows[key] for key in sorted(rows)]


steps = [
    ("POST '  Architecture DLR  '", lambda: post("  Architecture DLR  ")),
    ("POST '   '", lambda: post("   ")),
    ("POST 'architecture dlr'", lambda: post("architecture dlr")),
    ("GET 1", lambda: get(1)),
    ("GET 9", lambda: get(9)),
    ("PUT 1 'Plateforme'", lambda: put(1, "Plateforme")),
    ("DELETE 1", lambda: delete(1)),
    ("DELETE 1", lambda: delete(1)),
    ("POST 'Deuxième'", lambda: post("Deuxième")),
    ("LIST", list_items),
]
for label, action in steps:
    status, body = action()
    print(f"{label} -> {status} {body}")
