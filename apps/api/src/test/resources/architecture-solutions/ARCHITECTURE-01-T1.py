calls = {
    "client": ["gateway"],
    "gateway": ["booking", "search"],
    "booking": ["db"],
    "search": ["db"],
    "db": [],
}
owners = {
    "gateway": ["authentification", "routage"],
    "booking": ["regles de reservation"],
    "search": ["recherche", "stockage"],
    "db": ["stockage"],
}
required = ["authentification", "routage", "regles de reservation", "recherche", "stockage", "notifications"]

path = []


def visit(name):
    if name in path:
        return
    path.append(name)
    for target in calls[name]:
        visit(target)


visit("client")
print("Chemin: " + " -> ".join(path))
gaps = 0
for responsibility in required:
    holders = [name for name, items in owners.items() if responsibility in items]
    if not holders:
        verdict = "AUCUN PROPRIETAIRE"
        gaps += 1
    elif len(holders) > 1:
        verdict = f"CONFLIT ({', '.join(holders)})"
        gaps += 1
    else:
        verdict = holders[0]
    print(f"{responsibility}: {verdict}")
print(f"Ecarts: {gaps}")
