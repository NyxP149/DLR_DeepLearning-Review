NOW = 100
tokens = {
    "t-reader": {"user": "rita", "role": "reader", "expires": 500},
    "t-editor": {"user": "eddy", "role": "editor", "expires": 500},
    "t-admin": {"user": "adam", "role": "admin", "expires": 500},
    "t-old": {"user": "rita", "role": "reader", "expires": 50},
}
notes = {"n1": "rita", "n2": "eddy"}
requests = [
    ("t-reader", "read", "n1"),
    ("t-reader", "write", "n1"),
    ("t-editor", "write", "n2"),
    ("t-editor", "write", "n1"),
    ("t-admin", "delete", "n1"),
    ("t-old", "read", "n1"),
]
PERMISSIONS = {"reader": {"read"}, "editor": {"read", "write"}, "admin": {"read", "write", "delete"}}


def handle(token, action, note):
    identity = tokens.get(token)
    if identity is None or identity["expires"] <= NOW:
        return 401
    if action not in PERMISSIONS[identity["role"]]:
        return 403
    if action != "read" and identity["role"] != "admin" and notes[note] != identity["user"]:
        return 403
    return 200


for token, action, note in requests:
    print(f"{token} {action} {note} -> {handle(token, action, note)}")
