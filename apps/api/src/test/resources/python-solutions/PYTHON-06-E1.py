import json
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Order:
    customer: str
    amount: float

def paid_orders(payload: str) -> list[Order]:
    rows = json.loads(payload)
    return [
        Order(row["customer"], float(row["amount"]))
        for row in rows
        if row.get("status") == "PAID" and float(row.get("amount", 0)) > 0
    ]

payload = '[{"customer":"Ada","amount":120,"status":"PAID"},{"customer":"Linus","amount":80,"status":"PENDING"},{"customer":"Grace","amount":30,"status":"PAID"}]'
orders = paid_orders(payload)
total = sum(order.amount for order in orders)
assert len(orders) == 2
assert total == 150
print(f"Commandes payées: {len(orders)}")
print(f"Revenu: {total:.2f} EUR")
