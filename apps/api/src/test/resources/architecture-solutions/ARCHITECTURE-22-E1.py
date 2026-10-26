import math

RPS = 720
CAPACITY = 250
TARGET = 0.70
LATENCY_MS = 80
HOURLY_COST = 0.12
HOURS_PER_MONTH = 730

bare = math.ceil(RPS / CAPACITY)
print(f"Sans marge: {bare} instances, marge {bare * CAPACITY - RPS} req/s")
targeted = math.ceil(RPS / (CAPACITY * TARGET))
print(f"Utilisation cible 70%: {targeted} instances")
redundant = targeted + 1
print(f"Avec N+1: {redundant} instances")
degraded = round(100 * RPS / ((redundant - 1) * CAPACITY))
print(f"Panne d'une instance: {degraded}% d'utilisation")
concurrency = RPS * LATENCY_MS / 1000
print(f"Requêtes en cours: {concurrency:.1f} soit {concurrency / redundant:.1f} par instance")
print(f"Coût mensuel: {redundant * HOURLY_COST * HOURS_PER_MONTH:.2f} USD")
