SYSTEM = {
    "client": {"tech": "Angular", "owns": ["affichage"], "calls": ["api"]},
    "api": {"tech": "Spring Boot", "owns": ["règles"], "calls": ["data", "auth"]},
    "auth": {"tech": "Keycloak", "owns": ["identité"], "calls": []},
    "data": {"tech": "PostgreSQL", "owns": ["persistance"], "calls": []},
    "cache": {"tech": "Redis", "owns": ["persistance"], "calls": []},
}
REQUIRED = ["affichage", "règles", "persistance", "identité", "journalisation"]


def walk(start: str, seen: list[str] | None = None) -> list[str]:
    seen = [] if seen is None else seen
    if start in seen:
        return seen
    seen.append(start)
    for target in SYSTEM[start]["calls"]:
        walk(target, seen)
    return seen


print("Chemin: " + " -> ".join(walk("client")))
for duty in REQUIRED:
    owners = [name for name, info in SYSTEM.items() if duty in info["owns"]]
    if not owners:
        print(f"{duty}: AUCUN PROPRIÉTAIRE")
    elif len(owners) > 1:
        print(f"{duty}: CONFLIT ({', '.join(owners)})")
    else:
        print(f"{duty}: {owners[0]}")
called = {target for info in SYSTEM.values() for target in info["calls"]}
isolated = [name for name in SYSTEM if name != "client" and name not in called]
print("Composants isolés: " + (", ".join(isolated) or "aucun"))
