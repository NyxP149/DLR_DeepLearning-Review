def safe_ratio(a: float, b: float) -> float:
    if b == 0:
        raise ValueError("le denominateur ne peut pas etre nul")
    return a / b


def parse_port(text: str) -> int:
    try:
        port = int(text)
    except ValueError:
        raise ValueError(f"{text!r} n'est pas un entier") from None
    if not 1 <= port <= 65535:
        raise ValueError(f"{port} hors de [1, 65535]")
    return port


def add_item(item, bucket=None):
    bucket = [] if bucket is None else bucket
    bucket.append(item)
    return bucket


print("Ratio:", safe_ratio(5, 2))
try:
    safe_ratio(1, 0)
except ValueError as error:
    print("Erreur:", error)
print("Port:", parse_port("8080"))
for text in ("x1", "70000"):
    try:
        parse_port(text)
    except ValueError as error:
        print("Port invalide:", error)
print("Panier 1:", add_item("pomme"), "Panier 2:", add_item("poire"))
