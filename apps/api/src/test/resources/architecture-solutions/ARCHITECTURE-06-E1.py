seen = set()
def create(key: str) -> tuple[int, str]:
    if key in seen:
        return 200, "rejouée"
    seen.add(key)
    return 201, "créée"
print(*create("cmd-42"))
print(*create("cmd-42"))
