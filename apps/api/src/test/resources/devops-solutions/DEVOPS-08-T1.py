LOGS = [
    "09:15:01 gateway INFO requete ok",
    "09:15:20 cache ERROR memoire saturee",
    "09:15:22 auth ERROR cache indisponible",
    "09:15:23 auth ERROR cache indisponible",
    "09:15:25 gateway ERROR auth indisponible",
]
DEPENDS_ON = {"gateway": ["auth"], "auth": ["cache"], "cache": []}

errors = [line.split(" ", 3) for line in LOGS if " ERROR " in line]
first_time, first_service, _, first_message = errors[0]
counts = {}
for _, service, _, _ in errors:
    counts[service] = counts.get(service, 0) + 1
failing = set(counts)
cause = next(service for service in DEPENDS_ON if service in failing and not any(dep in failing for dep in DEPENDS_ON[service]))
dependents = [service for service in ("auth", "gateway") if cause in DEPENDS_ON[service] or any(dep in DEPENDS_ON and cause in DEPENDS_ON[dep] for dep in DEPENDS_ON[service])]
print(f"Premier incident: {first_time} {first_service} {first_message}")
print("Erreurs: " + ", ".join(f"{name}={counts[name]}" for name in sorted(counts)))
print(f"Cause probable: {cause}")
print(f"Plan: redemarrer {cause} -> " + " -> ".join(f"verifier {service}" for service in dependents))
