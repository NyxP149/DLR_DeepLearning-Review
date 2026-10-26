layers = ["MySQL", "DAO", "Service", "Controller", "UI"]
dependencies = [("DAO", "MySQL"), ("Service", "DAO"), ("Controller", "DAO"), ("UI", "Service"), ("DAO", "Controller")]

for number, layer in enumerate(layers, start=1):
    print(f"{number}. {layer}")
violations = 0
for caller, callee in dependencies:
    gap = layers.index(caller) - layers.index(callee)
    if gap == 1:
        verdict = "conforme"
    elif gap > 1:
        verdict = "saut de couche"
        violations += 1
    else:
        verdict = "dependance inversee"
        violations += 1
    print(f"{caller} -> {callee}: {verdict}")
print(f"Violations: {violations}")
