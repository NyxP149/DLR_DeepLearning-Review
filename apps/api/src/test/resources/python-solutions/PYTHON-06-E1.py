import json
from dataclasses import dataclass
from decimal import Decimal, InvalidOperation

VALID_STATUSES = {"PAID", "PENDING", "CANCELLED"}


@dataclass(frozen=True, slots=True)
class Order:
    customer: str
    amount: Decimal
    status: str


def validate(index: int, raw: dict):
    try:
        amount = Decimal(str(raw["amount"]))
    except (InvalidOperation, KeyError):
        return None, f"#{index}: montant invalide"
    if amount <= 0:
        return None, f"#{index}: montant négatif"
    if raw.get("status") not in VALID_STATUSES:
        return None, f"#{index}: statut inconnu"
    return Order(raw["customer"], amount, raw["status"]), None


payload = json.dumps([
    {"customer": "Ada", "amount": 120.5, "status": "PAID"},
    {"customer": "Linus", "amount": 80, "status": "PENDING"},
    {"customer": "Grace", "amount": "abc", "status": "PAID"},
    {"customer": "Ada", "amount": 80, "status": "PAID"},
    {"customer": "Bob", "amount": 10, "status": "WHAT"},
    {"customer": "Grace", "amount": 75, "status": "PAID"},
    {"customer": "Mia", "amount": -5, "status": "PAID"},
])

orders, rejected = [], []
for index, raw in enumerate(json.loads(payload)):
    order, reason = validate(index, raw)
    if reason:
        rejected.append(reason)
    elif order:
        orders.append(order)

paid = [order for order in orders if order.status == "PAID"]
revenue = sum((order.amount for order in paid), Decimal("0"))
by_customer: dict[str, Decimal] = {}
for order in paid:
    by_customer[order.customer] = by_customer.get(order.customer, Decimal("0")) + order.amount

assert len(paid) == 3
assert revenue == Decimal("275.50")

print(f"Commandes payées: {len(paid)}")
print(f"Revenu: {revenue:.2f} EUR")
print(f"Rejetées: {len(rejected)} ({', '.join(rejected)})")
ranking = sorted(by_customer.items(), key=lambda item: (-item[1], item[0]))
for position, (customer, amount) in enumerate(ranking, start=1):
    print(f"{position}. {customer} : {amount:.2f} EUR")
