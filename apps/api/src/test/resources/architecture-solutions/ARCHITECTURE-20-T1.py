boundaries = {
    "internet -> gateway": {"exposure": 3, "required": {"TLS", "authentification", "limitation de debit"}, "present": {"TLS"}},
    "gateway -> service": {"exposure": 2, "required": {"TLS", "autorisation"}, "present": {"TLS", "autorisation"}},
    "service -> base": {"exposure": 1, "required": {"TLS", "moindre privilege", "chiffrement"}, "present": {"moindre privilege"}},
}

gaps = {name: sorted(info["required"] - info["present"]) for name, info in boundaries.items()}
for name, missing in gaps.items():
    print(f"{name}: " + (", ".join(missing) if missing else "complete"))
print(f"Trous: {sum(len(missing) for missing in gaps.values())}")
print("Priorites:")
ranked = sorted(boundaries, key=lambda name: (-boundaries[name]["exposure"], -len(gaps[name]), name))
for position, name in enumerate(ranked, start=1):
    print(f"  {position}. {name} (exposition {boundaries[name]['exposure']}, {len(gaps[name])} trou(s))")
