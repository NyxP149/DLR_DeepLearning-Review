def safe_average(values: list[float]) -> float:
    if not values:
        raise ValueError("La liste ne peut pas être vide")
    return sum(values) / len(values)

try:
    print(f"Moyenne: {safe_average([10, 15, 20])}")
except ValueError as error:
    print(f"Erreur: {error}")
