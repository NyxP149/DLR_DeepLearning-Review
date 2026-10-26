import hashlib
import json

store: dict[str, tuple[str, dict]] = {}
orders: list[dict] = []


def fingerprint(payload: dict) -> str:
    return hashlib.sha256(json.dumps(payload, sort_keys=True).encode("utf-8")).hexdigest()


def create(key: str, payload: dict) -> tuple[int, dict]:
    digest = fingerprint(payload)
    if key in store:
        saved_digest, saved_response = store[key]
        if saved_digest == digest:
            return 200, saved_response
        return 409, {"error": "clé réutilisée avec un autre contenu"}
    order = {"id": len(orders) + 1, **payload}
    orders.append(order)
    store[key] = (digest, order)
    return 201, order


calls = [
    ("cmd-42", {"customer": 7, "total": 4000}),
    ("cmd-42", {"customer": 7, "total": 4000}),
    ("cmd-42", {"customer": 7, "total": 9999}),
    ("cmd-43", {"customer": 8, "total": 1500}),
]
for key, payload in calls:
    status, response = create(key, payload)
    print(key, "->", status, response)
print(f"Commandes créées: {len(orders)}")
