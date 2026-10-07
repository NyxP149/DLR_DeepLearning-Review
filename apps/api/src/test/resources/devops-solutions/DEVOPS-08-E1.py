LOGS = """10:02:11 INFO web requête /orders
10:02:12 ERROR db connexion refusée
10:02:13 ERROR api timeout vers db
10:02:14 ERROR api timeout vers db
10:02:15 ERROR web 502 depuis api
10:02:16 ERROR api timeout vers db
10:02:17 ERROR web 502 depuis api"""

DEPENDS_ON = {"web": "api", "api": "db"}


def parse(logs: str) -> list[tuple[str, str, str, str]]:
    entries = []
    for line in logs.splitlines():
        time, level, service, message = line.split(" ", 3)
        entries.append((time, level, service, message))
    return entries


entries = parse(LOGS)
errors = [entry for entry in entries if entry[1] == "ERROR"]

first = errors[0]
print(f"Premier incident: {first[0]} {first[2]} {first[3]}")

counts: dict[str, int] = {}
for _, _, service, _ in errors:
    counts[service] = counts.get(service, 0) + 1
print("Erreurs:", ", ".join(f"{service}={counts[service]}" for service in sorted(counts)))

failing = set(counts)
cause = next(service for service in sorted(failing) if DEPENDS_ON.get(service) not in failing)
print(f"Cause probable: {cause}")

dependents = {dependency: service for service, dependency in DEPENDS_ON.items()}
plan = [f"redémarrer {cause}"]
current = cause
while current in dependents:
    current = dependents[current]
    plan.append(f"vérifier {current}")
print("Plan:", " -> ".join(plan))
