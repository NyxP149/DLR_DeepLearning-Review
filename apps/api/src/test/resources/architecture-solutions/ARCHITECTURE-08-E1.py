NOW = 1000
TOKENS = {
    "t-reader": {"user": {"name": "rita", "role": "reader"}, "expires": 2000},
    "t-editor": {"user": {"name": "eva", "role": "editor"}, "expires": 2000},
    "t-admin": {"user": {"name": "adam", "role": "admin"}, "expires": 2000},
    "t-old": {"user": {"name": "eva", "role": "editor"}, "expires": 900},
}
PERMISSIONS = {"reader": {"read"}, "editor": {"read", "write"}, "admin": {"read", "write", "delete"}}
RESOURCES = {"doc-1": {"owner": "eva"}, "doc-2": {"owner": "adam"}}


def authenticate(token: str | None, now: int):
    entry = TOKENS.get(token)
    if entry is None or entry["expires"] <= now:
        return None
    return entry["user"]


def authorize(user: dict, action: str, resource: dict) -> int:
    if action not in PERMISSIONS.get(user["role"], set()):
        return 403
    if action in {"write", "delete"} and user["role"] != "admin" and resource["owner"] != user["name"]:
        return 403
    return 200


def handle(token: str | None, action: str, resource_id: str) -> int:
    user = authenticate(token, NOW)
    if user is None:
        return 401
    resource = RESOURCES.get(resource_id)
    if resource is None:
        return 404
    return authorize(user, action, resource)


requests = [
    ("t-reader", "read", "doc-1"),
    ("t-reader", "write", "doc-1"),
    ("t-editor", "write", "doc-1"),
    ("t-editor", "write", "doc-2"),
    ("t-admin", "delete", "doc-1"),
    ("t-old", "read", "doc-1"),
    (None, "read", "doc-1"),
]
for token, action, resource_id in requests:
    print(f"{token or 'sans jeton'} {action} {resource_id} -> {handle(token, action, resource_id)}")
