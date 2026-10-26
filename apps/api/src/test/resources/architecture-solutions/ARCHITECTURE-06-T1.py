import hashlib
import json

calls = [
    ("pay-1", {"order": 7, "amount": 4000}),
    ("pay-1", {"order": 7, "amount": 4000}),
    ("pay-1", {"order": 7, "amount": 9999}),
]
stored = {}
payments = []


def fingerprint(payload):
    return hashlib.sha256(json.dumps(payload, sort_keys=True).encode()).hexdigest()


def pay(key, payload):
    digest = fingerprint(payload)
    if key in stored:
        saved_digest, response = stored[key]
        if saved_digest != digest:
            return 409, {"error": "cle reutilisee avec un autre contenu"}
        return 200, response
    payment = {"id": len(payments) + 1, **payload}
    payments.append(payment)
    stored[key] = (digest, payment)
    return 201, payment


for key, payload in calls:
    status, body = pay(key, payload)
    print(f"{key} -> {status} {body}")
print(f"Paiements enregistres: {len(payments)}")
