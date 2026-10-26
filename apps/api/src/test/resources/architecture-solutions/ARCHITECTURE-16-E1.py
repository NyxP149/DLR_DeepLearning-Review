TTL = 2.0
source = {"product:7": {"name": "Clavier", "price": 50}}
cache: dict[str, tuple[dict, float]] = {}
stats = {"hit": 0, "miss": 0}


def get(key: str, now: float) -> dict:
    entry = cache.get(key)
    if entry and entry[1] > now:
        stats["hit"] += 1
        return entry[0]
    stats["miss"] += 1
    value = dict(source[key])
    cache[key] = (value, now + TTL)
    return value


def update(key: str, price: int) -> None:
    source[key]["price"] = price
    cache.pop(key, None)


def ratio() -> int:
    total = stats["hit"] + stats["miss"]
    return round(100 * stats["hit"] / total)


timeline = [
    (0.0, "get"), (1.0, "get"), (1.0, "update"), (1.0, "get"), (3.5, "get"), (3.6, "get"),
]
for now, action in timeline:
    if action == "get":
        print(f"t={now}: lu prix {get('product:7', now)['price']}")
    else:
        update("product:7", 60)
        print(f"t={now}: prix modifié à 60, entrée invalidée")
print(f"Succès: {stats['hit']}, échecs: {stats['miss']}, taux: {ratio()}%")
