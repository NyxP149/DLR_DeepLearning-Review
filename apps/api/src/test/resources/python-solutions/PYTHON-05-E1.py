from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Skill:
    name: str
    level: int

    def level_up(self) -> "Skill":
        return Skill(self.name, self.level + 1)

initial = Skill("Python", 1)
advanced = initial.level_up()
assert initial.level == 1
assert advanced.level == 2
print(f"{advanced.name}: niveau {advanced.level}")
