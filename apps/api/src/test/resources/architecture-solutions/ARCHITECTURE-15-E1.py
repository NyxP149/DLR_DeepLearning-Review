LATENCIES = {"db": 35, "api": 20, "network": 45, "ui": 30}
BUDGET = 150


def amdahl(share: float, speedup: float) -> float:
    return 1 / ((1 - share) + share / speedup)


total = sum(LATENCIES.values())
print(f"Latence totale: {total} ms")
print(f"Budget restant: {BUDGET - total} ms")
ordered = sorted(LATENCIES.items(), key=lambda item: (-item[1], item[0]))
for name, ms in ordered:
    print(f"{name}: {ms} ms ({100 * ms / total:.1f}%), gain si -50%: {ms / 2:.1f} ms")
print(f"À optimiser en premier: {ordered[0][0]}")
smallest = min(LATENCIES, key=LATENCIES.get)
print(f"Amdahl {smallest} infiniment rapide: x{amdahl(LATENCIES[smallest] / total, 1e9):.2f}")
