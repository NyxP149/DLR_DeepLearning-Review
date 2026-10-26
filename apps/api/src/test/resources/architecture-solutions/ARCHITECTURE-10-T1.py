suites = {
    "A": {"unitaires": (120, 6.0), "integration": (40, 30.0), "e2e": (10, 50.0)},
    "B": {"unitaires": (30, 1.0), "integration": (20, 20.0), "e2e": (50, 150.0)},
}

for name, levels in suites.items():
    total = sum(count for count, _ in levels.values())
    duration = sum(seconds for _, seconds in levels.values())
    print(f"Suite {name}: {total} tests, duree {duration:.1f} s")
    for level, (count, seconds) in levels.items():
        print(f"  {level}: {100 * count / total:.1f}% ({seconds:.1f} s)")
    healthy = levels["unitaires"][0] / total >= 0.6 and levels["e2e"][0] / total <= 0.1 and duration < 120
    print(f"  Verdict: {'SAINE' if healthy else 'FRAGILE'}")
