TTL = 2.0
store = {"EUR/USD": 1.10}
cache = {}
hits = misses = 0


def read(pair, now):
    global hits, misses
    entry = cache.get(pair)
    if entry and now < entry[1]:
        hits += 1
        return entry[0], "cache"
    misses += 1
    cache[pair] = (store[pair], now + TTL)
    return store[pair], "charge"


def update(pair, value, now):
    store[pair] = value
    cache.pop(pair, None)
    print(f"t={now}: {pair} modifie a {value}, entree invalidee")


for now in (0.0, 1.0):
    value, origin = read("EUR/USD", now)
    print(f"t={now}: lu {value} ({origin})")
update("EUR/USD", 1.25, 1.0)
for now in (1.0, 2.5, 3.1):
    value, origin = read("EUR/USD", now)
    print(f"t={now}: lu {value} ({origin})")
print(f"Succes: {hits}, echecs: {misses}, taux: {round(100 * hits / (hits + misses))}%")
