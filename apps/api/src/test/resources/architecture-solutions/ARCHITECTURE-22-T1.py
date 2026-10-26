import math

TRAFFIC = 900
CAPACITY = 200
TARGET = 0.7
LATENCY = 0.08
HOURLY_COST = 0.12
HOURS = 720

plain = math.ceil(TRAFFIC / CAPACITY)
print(f"Sans marge: {plain} instances, marge {plain * CAPACITY - TRAFFIC} req/s")
targeted = math.ceil(TRAFFIC / (CAPACITY * TARGET))
print(f"Utilisation cible {round(TARGET * 100)}%: {targeted} instances")
redundant = targeted + 1
print(f"Avec N+1: {redundant} instances")
after_failure = TRAFFIC / ((redundant - 1) * CAPACITY)
print(f"Panne d'une instance: {round(100 * after_failure)}% d'utilisation")
print(f"Requetes en cours: {TRAFFIC * LATENCY:.1f}")
print(f"Cout mensuel: {redundant * HOURLY_COST * HOURS:.2f} EUR")
