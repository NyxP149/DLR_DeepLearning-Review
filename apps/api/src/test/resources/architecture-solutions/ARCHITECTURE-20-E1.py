STANDARD = {"TLS", "authentification", "autorisation", "validation", "audit"}
BOUNDARIES = {
    "internet -> API": {"exposure": 3, "present": {"TLS", "authentification", "autorisation", "audit"}},
    "API -> base": {"exposure": 1, "present": {"authentification", "autorisation", "validation", "audit"}},
    "API -> paiement tiers": {"exposure": 2, "present": {"TLS", "authentification", "audit"}, "extra": {"secrets"}},
    "service -> service": {"exposure": 1, "present": {"TLS", "authentification", "autorisation", "validation", "audit"}},
}


def required(boundary: dict) -> set[str]:
    return STANDARD | boundary.get("extra", set())


def missing(boundary: dict) -> list[str]:
    return sorted(required(boundary) - boundary["present"])


total = 0
gaps = []
for name, boundary in BOUNDARIES.items():
    holes = missing(boundary)
    total += len(holes)
    if holes:
        gaps.append((name, boundary["exposure"], len(holes)))
        print(f"{name}: {', '.join(holes)}")
    else:
        print(f"{name}: complète")
print(f"Trous: {total}")
print("Priorités:")
for rank, (name, exposure, count) in enumerate(sorted(gaps, key=lambda item: (-item[1], -item[2], item[0])), start=1):
    print(f"  {rank}. {name} (exposition {exposure}, {count} trou(s))")
