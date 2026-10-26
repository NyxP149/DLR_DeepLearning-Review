tags = {}
sequence = 0


def post(name):
    global sequence
    clean = name.strip()
    if not clean:
        return 400, {"error": "nom vide"}
    if any(existing.lower() == clean.lower() for existing in tags.values()):
        return 409, {"error": "doublon"}
    sequence += 1
    tags[sequence] = clean
    return 201, {"id": sequence, "name": clean}


def get(tag_id):
    return (200, {"id": tag_id, "name": tags[tag_id]}) if tag_id in tags else (404, {"error": "introuvable"})


def put(tag_id, name):
    if tag_id not in tags:
        return 404, {"error": "introuvable"}
    if not name.strip():
        return 400, {"error": "nom vide"}
    tags[tag_id] = name.strip()
    return 200, {"id": tag_id, "name": tags[tag_id]}


def delete(tag_id):
    return (204, {}) if tags.pop(tag_id, None) is not None else (404, {"error": "introuvable"})


print("POST '  python  ' ->", *post("  python  "))
print("POST '   ' ->", *post("   "))
print("POST 'PYTHON' ->", *post("PYTHON"))
print("GET 1 ->", *get(1))
print("PUT 1 'Python 3' ->", *put(1, "Python 3"))
print("DELETE 1 ->", *delete(1))
print("DELETE 1 ->", *delete(1))
print("Liste:", list(tags.values()))
