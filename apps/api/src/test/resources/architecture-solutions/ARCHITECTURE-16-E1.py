cache = {"product:7": ("Clavier", 2)}
def tick(key: str):
    value, ttl = cache[key]
    ttl -= 1
    if ttl == 0:
        del cache[key]
        return "MISS"
    cache[key] = (value, ttl)
    return "HIT"
print(tick("product:7"))
print(tick("product:7"))
