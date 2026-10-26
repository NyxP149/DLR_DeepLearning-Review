items = {}


class Duplicate(Exception):
    pass


def create(body):
    name = (body or {}).get("name", "")
    if not name:
        return 422, {"detail": "name requis"}
    try:
        if name in items:
            raise Duplicate()
    except Duplicate:
        return 409, {"detail": "deja existant"}
    items[name] = {"name": name}
    return 201, items[name]


def handle(method, path, body=None):
    if method == "POST" and path == "/items":
        return create(body)
    if method == "GET" and path.startswith("/items/"):
        name = path.removeprefix("/items/")
        if name in items:
            return 200, items[name]
        return 404, {"detail": "introuvable"}
    return 404, {"detail": "route inconnue"}


calls = [
    ("POST", "/items", {"name": "cle"}),
    ("POST", "/items", {"name": "cle"}),
    ("POST", "/items", {"name": ""}),
    ("GET", "/items/cle", None),
    ("GET", "/items/zzz", None),
]
for method, path, body in calls:
    status, payload = handle(method, path, body)
    shown = f" {body}" if body is not None else ""
    print(f"{method} {path}{shown} -> {status} {payload}")
