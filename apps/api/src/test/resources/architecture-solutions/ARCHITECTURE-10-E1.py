HEALTHY = {"unitaires": (42, 0.05), "intégration": (12, 1.5), "contrats": (6, 0.5), "e2e": (3, 20.0)}
INVERTED = {"unitaires": (10, 0.05), "intégration": (5, 1.5), "contrats": (0, 0.5), "e2e": (15, 20.0)}
MIN_UNIT_SHARE, MAX_E2E_SHARE, MAX_SECONDS = 60.0, 10.0, 120.0


def report(name: str, suite: dict) -> None:
    total = sum(count for count, _ in suite.values())
    shares = {level: 100 * count / total for level, (count, _) in suite.items()}
    durations = {level: count * seconds for level, (count, seconds) in suite.items()}
    total_seconds = sum(durations.values())
    print(f"{name}: {total} tests, durée {total_seconds:.1f} s")
    for level in suite:
        print(f"  {level}: {shares[level]:.1f}% ({durations[level]:.1f} s)")
    problems = []
    if shares["unitaires"] < MIN_UNIT_SHARE:
        problems.append("unitaires sous 60%")
    if shares["e2e"] > MAX_E2E_SHARE:
        problems.append("e2e au-dessus de 10%")
    if total_seconds > MAX_SECONDS:
        problems.append("durée au-dessus de 120 s")
    print("  Verdict: SAINE" if not problems else f"  Verdict: À REVOIR ({'; '.join(problems)})")


report("Suite A", HEALTHY)
report("Suite B", INVERTED)
