def safe_average(values: list[float]) -> float:
    if not values:
        raise ValueError("La liste ne peut pas être vide")
    return sum(values) / len(values)


def parse_age(text: str) -> int:
    try:
        age = int(text)
    except ValueError as error:
        raise ValueError(f"{text!r} n'est pas un entier") from error
    if not 0 <= age <= 150:
        raise ValueError(f"{age} hors de [0, 150]")
    return age


def add_tag(tag: str, tags: list[str] | None = None) -> list[str]:
    if tags is None:
        tags = []
    tags.append(tag)
    return tags


try:
    print(f"Moyenne: {safe_average([10, 15, 20])}")
    print(f"Moyenne: {safe_average([])}")
except ValueError as error:
    print(f"Erreur: {error}")

for text in ("42", "abc", "200"):
    try:
        print(f"Age: {parse_age(text)}")
    except ValueError as error:
        print(f"Age invalide: {error}")

first = add_tag("a")
second = add_tag("b")
print(f"Liste 1: {first} Liste 2: {second}")
