SCENARIOS = [(9, 1), (8, 2), (3, 1)]
TOTAL = 20
LIMIT_PERCENT = 5
for stable, canary in SCENARIOS:
    share = round(100 * canary / (stable + canary))
    print(f"stable={stable} canary={canary} -> canary {share} %")
biggest = max(count for count in range(TOTAL + 1) if 100 * count / TOTAL <= LIMIT_PERCENT)
print(f"Max {LIMIT_PERCENT} % sur {TOTAL} pods : {biggest} canary")
