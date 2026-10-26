LAYERS = ["PostgreSQL", "Repository", "Service", "API REST", "Interface"]
CALLS = [
    ("Repository", "PostgreSQL"),
    ("Service", "Repository"),
    ("API REST", "Service"),
    ("Interface", "API REST"),
    ("Interface", "Repository"),
    ("Repository", "Service"),
]

for number, layer in enumerate(LAYERS, start=1):
    print(f"{number}. {layer}")
level = {layer: number for number, layer in enumerate(LAYERS, start=1)}
violations = 0
for caller, callee in CALLS:
    gap = level[caller] - level[callee]
    if gap == 1:
        verdict = "conforme"
    elif gap > 1:
        verdict = f"SAUT DE COUCHE ({gap - 1} couche(s) ignorée(s))"
        violations += 1
    else:
        verdict = "DÉPENDANCE INVERSÉE"
        violations += 1
    print(f"{caller} -> {callee}: {verdict}")
print(f"Violations: {violations}")
