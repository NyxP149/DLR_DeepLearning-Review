TRANSITIONS = {
    "DRAFT": {"CONFIRMED", "CANCELLED"},
    "CONFIRMED": {"SHIPPED", "CANCELLED"},
    "SHIPPED": set(),
    "CANCELLED": set(),
}
ORDERS = [
    {"id": 42, "customer_id": 7, "lines": [2500, 1500], "status": "DRAFT"},
    {"id": 43, "customer_id": 7, "lines": [], "status": "DRAFT"},
    {"id": 44, "customer_id": 8, "lines": [1000, -50], "status": "PENDING"},
]


def violations(order: dict) -> list[str]:
    found = []
    if not order["lines"]:
        found.append("aucune ligne")
    if any(price <= 0 for price in order["lines"]):
        found.append("prix non positif")
    if order["status"] not in TRANSITIONS:
        found.append("statut inconnu")
    return found


def euros(cents: int) -> str:
    whole, rest = divmod(cents, 100)
    return f"{whole}.{rest:02d}"


def can_move(current: str, target: str) -> bool:
    return target in TRANSITIONS.get(current, set())


for order in ORDERS:
    problems = violations(order)
    if problems:
        print(f"Commande {order['id']}: invalide ({', '.join(problems)})")
    else:
        print(f"Commande {order['id']}: valide, total {euros(sum(order['lines']))} EUR")
print(f"DRAFT -> SHIPPED: {'autorisé' if can_move('DRAFT', 'SHIPPED') else 'refusé'}")
print(f"CONFIRMED -> SHIPPED: {'autorisé' if can_move('CONFIRMED', 'SHIPPED') else 'refusé'}")
