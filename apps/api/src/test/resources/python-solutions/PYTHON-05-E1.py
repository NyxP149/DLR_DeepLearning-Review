from dataclasses import dataclass, replace, FrozenInstanceError


@dataclass(frozen=True, slots=True)
class Skill:
    name: str
    level: int

    def __post_init__(self):
        if not 1 <= self.level <= 5:
            raise ValueError("niveau doit etre entre 1 et 5")

    def level_up(self) -> "Skill":
        return replace(self, level=self.level + 1)


original = Skill("Python", 1)
upgraded = original.level_up()
assert original.level == 1
assert upgraded.level == 2
print(f"{upgraded.name}: niveau {upgraded.level}")
print(f"Original inchange: niveau {original.level}")

try:
    Skill("Bug", 9)
except ValueError as error:
    print(f"Refuse: {error}")

print(f"Egalite par valeur: {Skill('A', 1) == Skill('A', 1)}")
print(f"Dans un set: {len({Skill('A', 1), Skill('A', 1)})}")

try:
    original.level = 3
except (FrozenInstanceError, AttributeError):
    print("Modification refusee: True")
