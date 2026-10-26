import csv
import io
from decimal import Decimal

DATA = """produit,quantite,prix
stylo,10,1.20
cahier,abc,2.50
stylo,5,1.20
,3,4.00
cahier,2,2.50
gomme,-1,0.80
"""
rows = list(csv.DictReader(io.StringIO(DATA)))
rejected = []
totals = {}
for number, row in enumerate(rows, start=2):
    if not row["produit"]:
        rejected.append(f"ligne {number}: produit manquant")
        continue
    try:
        quantity = int(row["quantite"])
    except ValueError:
        rejected.append(f"ligne {number}: quantite invalide")
        continue
    if quantity < 0:
        rejected.append(f"ligne {number}: quantite negative")
        continue
    amount = quantity * Decimal(row["prix"])
    totals[row["produit"]] = totals.get(row["produit"], Decimal("0")) + amount
print(f"Lignes lues: {len(rows)}")
print(f"Rejetees: {len(rejected)} ({', '.join(rejected)})")
print("Total par produit: " + ", ".join(f"{name}={total:.2f}" for name, total in sorted(totals.items())))
