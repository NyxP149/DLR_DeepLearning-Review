metrics = {"p95_ms": 340, "error_rate": 0.6, "bundle_kb": 480, "lint_warnings": 0}
COMMIT = "9f8e7d6c5b4a3210"
VERSION = "1.8.0"
thresholds = {"p95_ms": 300, "error_rate": 1, "bundle_kb": 500, "lint_warnings": 0}

failures = 0
for name, limit in thresholds.items():
    value = metrics[name]
    if value <= limit:
        print(f"{name}: {value} <= {limit} -> OK")
    else:
        print(f"{name}: {value} > {limit} -> ECHEC")
        failures += 1
if failures:
    print(f"Gate: REFUSE ({failures} echec(s))")
else:
    print("Gate: ACCEPTE")
    print(f"Artefact: api-{VERSION}-{COMMIT[:7]}")
