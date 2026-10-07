import csv
import io
from decimal import Decimal, InvalidOperation

RAW = """pays,client,montant
FR,Ada,100.50
DE,Linus,80
FR,Grace,abc
,Alan,20
FR,Hedy,50
DE,Ken,-5"""


def parse_rows(raw: str) -> tuple[list[tuple[str, Decimal]], list[str]]:
    valid: list[tuple[str, Decimal]] = []
    rejected: list[str] = []
    reader = csv.DictReader(io.StringIO(raw))
    for line, row in enumerate(reader, start=2):
        country = row["pays"].strip()
        if not country:
            rejected.append(f"ligne {line}: pays manquant")
            continue
        try:
            amount = Decimal(row["montant"])
        except InvalidOperation:
            rejected.append(f"ligne {line}: montant invalide")
            continue
        if amount < 0:
            rejected.append(f"ligne {line}: montant négatif")
            continue
        valid.append((country, amount))
    return valid, rejected


def totals(rows: list[tuple[str, Decimal]]) -> dict[str, Decimal]:
    result: dict[str, Decimal] = {}
    for country, amount in rows:
        result[country] = result.get(country, Decimal("0")) + amount
    return result


valid, rejected = parse_rows(RAW)
print(f"Lignes lues: {len(valid) + len(rejected)}")
print(f"Rejetées: {len(rejected)} ({', '.join(rejected)})")
print("Total par pays:", ", ".join(f"{country}={total:.2f}" for country, total in sorted(totals(valid).items())))
