SLO = 0.995
DAYS = 30
MIN_REMAINING = 0.25
SITUATIONS = [
    ("Tout va bien", {"gateway": "healthy", "orders": "healthy", "billing": "healthy"}, 80),
    ("Service dégradé", {"gateway": "healthy", "orders": "healthy", "billing": "degraded"}, 80),
    ("Budget faible", {"gateway": "healthy", "orders": "healthy", "billing": "healthy"}, 180),
]


def budget_minutes() -> float:
    return DAYS * 24 * 60 * (1 - SLO)


def decide(services: dict, consumed: float) -> tuple[str, str]:
    unhealthy = [name for name, state in services.items() if state != "healthy"]
    remaining = (budget_minutes() - consumed) / budget_minutes()
    if unhealthy:
        return "STOP", f"{', '.join(unhealthy)} à traiter"
    if remaining < MIN_REMAINING:
        return "STOP", f"budget restant {round(remaining * 100)}% sous {round(MIN_REMAINING * 100)}%"
    return "GO", f"tous les services sont sains, budget restant {round(remaining * 100)}%"


total = budget_minutes()
print(f"Budget d'erreur: {total:.0f} minutes sur {DAYS} jours")
for label, services, consumed in SITUATIONS:
    remaining = round(100 * (total - consumed) / total)
    verdict, reason = decide(services, consumed)
    print(f"{label}: consommé {consumed} min (restant {remaining}%)")
    print(f"  Promotion: {verdict} - {reason}")
