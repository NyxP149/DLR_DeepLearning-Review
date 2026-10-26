bookings = [
    {"id": 7, "nights": 2, "rate_cents": 12000, "status": "CONFIRMED"},
    {"id": 8, "nights": 0, "rate_cents": 9000, "status": "DRAFT"},
    {"id": 9, "nights": 1, "rate_cents": -5, "status": "ARCHIVED"},
]
transitions = [("DRAFT", "CONFIRMED"), ("CONFIRMED", "CANCELLED"), ("CANCELLED", "CONFIRMED")]
ALLOWED = {("DRAFT", "CONFIRMED"), ("CONFIRMED", "CANCELLED"), ("DRAFT", "CANCELLED")}

for booking in bookings:
    problems = []
    if booking["nights"] < 1:
        problems.append("aucune nuit")
    if booking["rate_cents"] <= 0:
        problems.append("prix non positif")
    if booking["status"] not in {"DRAFT", "CONFIRMED", "CANCELLED"}:
        problems.append("statut inconnu")
    if problems:
        print(f"Reservation {booking['id']}: invalide ({', '.join(problems)})")
    else:
        total = booking["nights"] * booking["rate_cents"]
        print(f"Reservation {booking['id']}: valide, total {total // 100}.{total % 100:02d} EUR")
for source, target in transitions:
    print(f"{source} -> {target}: {'autorise' if (source, target) in ALLOWED else 'refuse'}")
