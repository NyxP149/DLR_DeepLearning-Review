members = [
    {"name": "Linus", "active": True, "role": "dev", "years": 5},
    {"name": "Grace", "active": False, "role": "admin", "years": 12},
    {"name": "Ada", "active": True, "role": "dev", "years": 8},
    {"name": "Nyx", "active": True, "role": "ops", "years": 2},
]

actifs = sorted(m["name"] for m in members if m["active"])
print("Actifs: " + ", ".join(actifs))

roles = sorted({m["role"] for m in members})
print("Roles uniques:", roles)

par_role = {}
for m in members:
    par_role[m["role"]] = par_role.get(m["role"], 0) + 1
print("Par role:", dict(sorted(par_role.items())))

plus_ancien = max(members, key=lambda m: m["years"])
print(f"Plus ancien: {plus_ancien['name']} ({plus_ancien['years']} ans)")

print("Source intacte:", len(members))
