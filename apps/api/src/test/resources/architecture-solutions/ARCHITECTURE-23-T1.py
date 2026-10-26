SLO = 99.9
DAYS = 30
situations = {
    "calme": {"consumed": 10, "services": {"api": True, "db": True}},
    "budget bas": {"consumed": 38, "services": {"api": True, "db": True}},
    "service malade": {"consumed": 5, "services": {"api": True, "db": False}},
}

budget = DAYS * 24 * 60 * (100 - SLO) / 100
print(f"Budget d'erreur: {budget:.1f} minutes sur {DAYS} jours")
for name, situation in situations.items():
    remaining = round(100 * (budget - situation["consumed"]) / budget)
    sick = [service for service, healthy in situation["services"].items() if not healthy]
    print(f"{name}: consomme {situation['consumed']} min (restant {remaining}%)")
    if sick:
        print(f"  Services non sains: {', '.join(sick)}")
    if sick:
        print("  Promotion: NO GO - service non sain")
    elif remaining < 25:
        print(f"  Promotion: NO GO - budget restant {remaining}%")
    else:
        print(f"  Promotion: GO - tous les services sont sains, budget restant {remaining}%")
