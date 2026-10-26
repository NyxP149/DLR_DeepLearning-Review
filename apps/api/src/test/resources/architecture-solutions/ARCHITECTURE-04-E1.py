STACKS = {
    "Spring-Angular": {"team": 5, "speed": 3, "ops": 4},
    "FastAPI-TypeScript": {"team": 3, "speed": 5, "ops": 3},
    "Rust-WebAssembly": {"team": 1, "speed": 2, "ops": 2},
}
WEIGHTS = {"team": 2, "speed": 1, "ops": 2}
MIN_TEAM = 3


def score(notes: dict, weights: dict) -> int:
    return sum(notes[criterion] * weight for criterion, weight in weights.items())


def ranking(weights: dict) -> list[tuple[str, int]]:
    eligible = [(name, score(notes, weights)) for name, notes in STACKS.items() if notes["team"] >= MIN_TEAM]
    return sorted(eligible, key=lambda item: (-item[1], item[0]))


rejected = [name for name, notes in STACKS.items() if notes["team"] < MIN_TEAM]
print("Écartées: " + (", ".join(rejected) or "aucune"))
base = ranking(WEIGHTS)
for position, (name, value) in enumerate(base, start=1):
    print(f"{position}. {name}: {value}")
print(f"Choix: {base[0][0]}")
alternative = ranking({"team": 2, "speed": 5, "ops": 2})
print(f"Sensibilité: le choix devient {alternative[0][0]}")
print(f"Décision robuste: {alternative[0][0] == base[0][0]}")
