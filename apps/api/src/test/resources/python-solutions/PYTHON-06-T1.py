from decimal import Decimal, InvalidOperation

lines = [
    "1;Ada;120.50;PAID",
    "2;Nyx;abc;PAID",
    "3;Linus;80.00;PAID",
    "4;Ada;30.00;PENDING",
    "5;Nyx;45.00;PAID",
    "6;Zoe;-5;PAID",
    "7;Max;60.00;ARCHIVED",
]
STATUSES = {"PAID", "PENDING", "CANCELLED"}
paid = []
rejected = []
for line in lines:
    number, client, amount, status = line.split(";")
    try:
        value = Decimal(amount)
    except InvalidOperation:
        rejected.append(f"#{number}: montant invalide")
        continue
    if value <= 0:
        rejected.append(f"#{number}: montant negatif")
        continue
    if status not in STATUSES:
        rejected.append(f"#{number}: statut inconnu")
        continue
    if status == "PAID":
        paid.append((client, value))

revenue = {}
for client, value in paid:
    revenue[client] = revenue.get(client, Decimal("0")) + value
ranking = sorted(revenue.items(), key=lambda item: (-item[1], item[0]))
print(f"Reservations payees: {len(paid)}")
print(f"Revenu: {sum(revenue.values()):.2f} EUR")
print(f"Rejetees: {len(rejected)} ({', '.join(rejected)})")
for rank, (client, value) in enumerate(ranking, start=1):
    print(f"{rank}. {client} : {value:.2f} EUR")
