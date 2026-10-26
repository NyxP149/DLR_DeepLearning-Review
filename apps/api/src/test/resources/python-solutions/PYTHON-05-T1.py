from dataclasses import dataclass, replace, FrozenInstanceError


@dataclass(frozen=True, slots=True)
class Money:
    cents: int
    currency: str

    def __post_init__(self):
        if self.cents < 0:
            raise ValueError("montant negatif")

    def add(self, other: "Money") -> "Money":
        return replace(self, cents=self.cents + other.cents)


original = Money(1000, "EUR")
total = original.add(Money(500, "EUR"))
print(f"Total: {total.cents} {total.currency}")
print(f"Original inchange: {original.cents} {original.currency}")
try:
    Money(-1, "EUR")
except ValueError as error:
    print(f"Refuse: {error}")
print("Egalite par valeur:", Money(5, "EUR") == Money(5, "EUR"))
print("Dans un set:", len({Money(5, "EUR"), Money(5, "EUR")}))
try:
    original.cents = 1
except FrozenInstanceError:
    print("Modification refusee:", True)
