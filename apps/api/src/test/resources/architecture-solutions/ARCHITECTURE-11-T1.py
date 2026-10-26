stack = {
    "proxy": {"depends_on": ["app"], "ports": [443], "healthcheck": True},
    "app": {"depends_on": ["db", "queue"], "ports": [], "healthcheck": True},
    "db": {"depends_on": [], "ports": [5432], "healthcheck": True},
    "queue": {"depends_on": [], "ports": [], "healthcheck": False},
}


def waves(services):
    remaining = {name: set(info["depends_on"]) for name, info in services.items()}
    result = []
    while remaining:
        ready = sorted(name for name, deps in remaining.items() if not deps)
        if not ready:
            raise ValueError("cycle")
        result.append(ready)
        for name in ready:
            del remaining[name]
        for deps in remaining.values():
            deps.difference_update(ready)
    return result


for number, wave in enumerate(waves(stack), start=1):
    print(f"Vague {number}: {', '.join(wave)}")
try:
    waves({"a": {"depends_on": ["b"]}, "b": {"depends_on": ["a"]}})
except ValueError as error:
    print(f"Erreur: {error}")
for name in sorted(stack):
    if name != "proxy" and stack[name]["ports"]:
        print(f"ALERTE port publie: {name} {stack[name]['ports']}")
needed = {dependency for info in stack.values() for dependency in info["depends_on"]}
for name in sorted(needed):
    if not stack[name]["healthcheck"]:
        print(f"ALERTE healthcheck manquant: {name}")
