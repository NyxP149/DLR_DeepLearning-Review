RULES = {
    "coverage": ("min", 80),
    "critical_vulns": ("max", 0),
    "duplication": ("max", 5),
    "failed_tests": ("max", 0),
}


def check(name: str, value: int) -> tuple[bool, str]:
    kind, limit = RULES[name]
    if kind == "min":
        ok = value >= limit
        operator = ">=" if ok else "<"
    else:
        ok = value <= limit
        operator = "<=" if ok else ">"
    return ok, f"{name}: {value} {operator} {limit} -> {'OK' if ok else 'ÉCHEC'}"


def artifact_name(version: str, commit: str) -> str:
    return f"dlr-api-{version}+{commit[:7]}"


def run(commit: str, metrics: dict) -> None:
    print(f"Build {commit[:7]}")
    failures = 0
    for name in RULES:
        ok, line = check(name, metrics[name])
        print(line)
        if not ok:
            failures += 1
    if failures:
        print(f"Gate: REFUSÉ ({failures} échec(s)) - artefact non publié")
    else:
        print(f"Gate: ACCEPTÉ - artefact {artifact_name('1.4.0', commit)} publié")


run("a1b2c3d4e5f6", {"coverage": 72, "critical_vulns": 0, "duplication": 7, "failed_tests": 0})
run("9f8e7d6c5b4a", {"coverage": 85, "critical_vulns": 0, "duplication": 3, "failed_tests": 0})
