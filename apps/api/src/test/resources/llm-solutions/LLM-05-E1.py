cases = [("JVM", "JVM"), ("bytecode", "bytecode"), ("JDK", "JRE")]
correct = sum(expected == actual for expected, actual in cases)
print(f"Exactitude: {correct}/{len(cases)}")
print(f"Taux: {correct / len(cases):.0%}")
