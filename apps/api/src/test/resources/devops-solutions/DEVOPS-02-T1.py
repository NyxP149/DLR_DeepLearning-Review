services = {
    "web": {"ports": [8080], "networks": ["front"]},
    "admin": {"ports": [8080, 9000], "networks": ["front"]},
    "api": {"ports": [], "networks": ["front", "backend"]},
}
declared_networks = {"front"}

problems = []
by_port = {}
for name, service in services.items():
    for port in service["ports"]:
        by_port.setdefault(port, []).append(name)
for port, names in sorted(by_port.items()):
    if len(names) > 1:
        problems.append(f"ports: conflit {port} ({', '.join(sorted(names))})")
for name, service in services.items():
    for network in service["networks"]:
        if network not in declared_networks:
            problems.append(f"reseau inconnu: {network} (service {name})")
for problem in problems:
    print(problem)
print("Verdict: OK" if not problems else f"Verdict: {len(problems)} probleme(s)")
