values = [4, 9, 1, 7, 3, 9, 8]


def naive(items):
    comparisons = 0
    for i in range(len(items)):
        for j in range(i + 1, len(items)):
            comparisons += 1
            if items[i] == items[j]:
                return items[i], comparisons
    return None, comparisons


def hashed(items):
    seen = set()
    lookups = 0
    for item in items:
        lookups += 1
        if item in seen:
            return item, lookups
        seen.add(item)
    return None, lookups


value, comparisons = naive(values)
print(f"Naif: doublon {value} apres {comparisons} comparaisons")
value, lookups = hashed(values)
print(f"Ensemble: doublon {value} apres {lookups} consultations")
print(f"Gain: {comparisons - lookups} operations")
