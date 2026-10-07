members = [
    {"name": "Linus", "active": True},
    {"name": "Grace", "active": False},
    {"name": "Ada", "active": True},
]

active_names = sorted(member["name"] for member in members if member["active"] )
print(f"Actifs: {', '.join(active_names)}")
