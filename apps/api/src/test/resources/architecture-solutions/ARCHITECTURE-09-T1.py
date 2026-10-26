stock = {"units": 40, "version": 1}


def debit(state, amount, expected_version):
    if state["version"] != expected_version:
        return "CONFLIT"
    if amount > state["units"]:
        return "SOLDE"
    state["units"] -= amount
    state["version"] += 1
    return "OK"


def client(name, amount, first_read_version):
    version = first_read_version
    for attempt in range(1, 4):
        result = debit(stock, amount, version)
        if result == "OK":
            print(f"{name}: OK apres {attempt} essai(s)")
            return
        if result == "SOLDE":
            print(f"{name}: SOLDE")
            return
        version = stock["version"]
    print(f"{name}: abandon")


seen = stock["version"]
client("A", 10, seen)
client("B", 5, seen)
print(f"Stock: {stock}")
print(f"Retrait 500: {debit(stock, 500, stock['version'])}")
