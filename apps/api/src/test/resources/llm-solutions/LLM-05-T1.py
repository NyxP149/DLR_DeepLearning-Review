expected = {"q1": "A", "q2": "B", "q3": "A", "q4": "C", "q5": "B", "q6": "A", "q7": "C", "q8": "B"}
strategy_a = {"q1": "A", "q2": "B", "q3": "B", "q4": "C", "q5": "A", "q6": "A", "q7": "A", "q8": "B"}
strategy_b = {"q1": "A", "q2": "B", "q3": "A", "q4": "B", "q5": "B", "q6": "A", "q7": "C", "q8": "B"}


def correct(strategy):
    return {case for case, answer in expected.items() if strategy[case] == answer}


ok_a, ok_b = correct(strategy_a), correct(strategy_b)
fixed = sorted(ok_b - ok_a)
broken = sorted(ok_a - ok_b)
n = len(expected)
print(f"Strategie A: {len(ok_a)}/{n} ({round(100 * len(ok_a) / n)}%)")
print(f"Strategie B: {len(ok_b)}/{n} ({round(100 * len(ok_b) / n)}%)")
print(f"B corrige {len(fixed)} cas et en degrade {len(broken)} -> gain net: {len(fixed) - len(broken):+d}")
print(f"Cas degrades: {', '.join(broken) if broken else 'aucun'}")
print(f"Echantillon trop petit pour conclure: {n < 30} (n={n} < 30)")
