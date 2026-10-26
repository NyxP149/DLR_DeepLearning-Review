EXPECTED = {"q1": "JVM", "q2": "bytecode", "q3": "JDK", "q4": "JRE", "q5": "heap", "q6": "GC"}
STRATEGY_A = {"q1": "JVM", "q2": "bytecode", "q3": "JRE", "q4": "JRE", "q5": "stack", "q6": "GC"}
STRATEGY_B = {"q1": "JVM", "q2": "bytecode", "q3": "JDK", "q4": "JDK", "q5": "heap", "q6": "GC"}
MIN_CASES = 30


def correct_cases(answers: dict) -> set:
    return {case for case, expected in EXPECTED.items() if answers[case] == expected}


a_ok = correct_cases(STRATEGY_A)
b_ok = correct_cases(STRATEGY_B)
total = len(EXPECTED)
for name, ok in (("A", a_ok), ("B", b_ok)):
    print(f"Stratégie {name}: {len(ok)}/{total} ({round(100 * len(ok) / total)}%)")
fixed = b_ok - a_ok
broken = a_ok - b_ok
print(f"B corrige {len(fixed)} cas et en dégrade {len(broken)} -> gain net: {len(fixed) - len(broken):+d}")
print("Cas dégradés: " + (", ".join(sorted(broken)) or "aucun"))
print(f"Échantillon trop petit pour conclure: {total < MIN_CASES} (n={total} < {MIN_CASES})")
