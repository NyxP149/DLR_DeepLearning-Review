DEPENDS = {"web": ["api", "cdn"], "api": ["db", "cache"], "db": [], "cache": [], "cdn": []}
FAILED_AT = {"web": 4, "api": 2, "db": 1}
roots = [name for name in FAILED_AT if not any(dep in FAILED_AT for dep in DEPENDS[name])]
root = roots[0]
impacted = sorted((name for name in FAILED_AT if name != root), key=lambda name: FAILED_AT[name])
print(f"Cause racine: {root}")
print("Impactés: " + ", ".join(impacted))
