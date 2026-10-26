STACK = {
    "web": {"depends_on": ["api"], "ports": [8080], "healthcheck": True, "entrypoint": True},
    "api": {"depends_on": ["db", "cache"], "ports": [], "healthcheck": True, "entrypoint": False},
    "db": {"depends_on": [], "ports": [5432], "healthcheck": True, "entrypoint": False},
    "cache": {"depends_on": [], "ports": [], "healthcheck": False, "entrypoint": False},
}
CYCLE = {"a": {"depends_on": ["b"]}, "b": {"depends_on": ["a"]}}


def waves(stack: dict) -> list[list[str]]:
    started: set[str] = set()
    pending = set(stack)
    result = []
    while pending:
        ready = sorted(name for name in pending if set(stack[name]["depends_on"]) <= started)
        if not ready:
            raise ValueError("cycle")
        result.append(ready)
        started |= set(ready)
        pending -= set(ready)
    return result


for number, wave in enumerate(waves(STACK), start=1):
    print(f"Vague {number}: {', '.join(wave)}")
try:
    waves(CYCLE)
except ValueError as error:
    print(f"Erreur: {error}")
for name, service in STACK.items():
    if service["ports"] and not service["entrypoint"]:
        print(f"ALERTE port publié: {name} {service['ports']}")
expected = {dep for service in STACK.values() for dep in service["depends_on"]}
for name, service in STACK.items():
    if name in expected and not service["healthcheck"]:
        print(f"ALERTE healthcheck manquant: {name}")
