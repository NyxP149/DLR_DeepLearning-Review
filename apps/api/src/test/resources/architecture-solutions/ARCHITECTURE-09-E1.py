account = {"balance": 100, "version": 3}


def read() -> dict:
    return dict(account)


def debit(amount: int, expected_version: int) -> str:
    if expected_version != account["version"]:
        return "CONFLIT"
    if account["balance"] < amount:
        return "SOLDE"
    account["balance"] -= amount
    account["version"] += 1
    return "OK"


def debit_with_retry(amount: int, snapshot: dict, max_attempts: int = 3) -> str:
    for attempt in range(1, max_attempts + 1):
        result = debit(amount, snapshot["version"])
        if result == "OK":
            return f"OK après {attempt} essai(s)"
        if result == "SOLDE":
            return "SOLDE"
        snapshot = read()
    return "CONFLIT PERSISTANT"


snapshot_a = read()
snapshot_b = read()
print("A: " + debit_with_retry(30, snapshot_a))
print("B: " + debit_with_retry(30, snapshot_b))
print("Compte:", account)
print("Solde insuffisant: " + debit_with_retry(500, read()))
