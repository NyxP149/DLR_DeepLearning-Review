latencies = {"db": 35, "api": 20, "network": 45, "ui": 30}
total = sum(latencies.values())
budget = 150
print("Latence totale:", total, "ms")
print("Budget restant:", budget - total, "ms")
