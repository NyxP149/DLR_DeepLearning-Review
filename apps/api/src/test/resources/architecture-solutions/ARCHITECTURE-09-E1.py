account = {"balance": 100, "version": 3}
def debit(amount: int, expected_version: int) -> str:
    if expected_version != account["version"]:
        return "CONFLIT"
    account["balance"] -= amount
    account["version"] += 1
    return "OK"
print(debit(30, 3), account)
print(debit(20, 3), account)
