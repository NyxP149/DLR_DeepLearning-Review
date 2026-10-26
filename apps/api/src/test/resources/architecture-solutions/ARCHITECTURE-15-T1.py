components = {"gateway": 20, "service": 60, "database": 90, "cache": 10}
BUDGET = 200

total = sum(components.values())
print(f"Latence totale: {total} ms")
print(f"Budget restant: {BUDGET - total} ms")
for name, duration in sorted(components.items(), key=lambda item: -item[1]):
    print(f"{name}: {duration} ms ({100 * duration / total:.1f}%), gain si -50%: {duration * 0.5:.1f} ms")
smallest = min(components.values())
share = smallest / total
print(f"Gain maximal (Amdahl) en supprimant le plus petit: {100 * share:.1f}% de la latence, soit {smallest} ms")
