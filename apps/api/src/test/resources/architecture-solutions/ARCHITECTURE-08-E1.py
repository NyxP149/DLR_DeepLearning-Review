permissions = {"reader": {"read"}, "editor": {"read", "write"}}
def authorize(role: str, action: str) -> int:
    return 200 if action in permissions.get(role, set()) else 403
print("reader/write:", authorize("reader", "write"))
print("editor/write:", authorize("editor", "write"))
