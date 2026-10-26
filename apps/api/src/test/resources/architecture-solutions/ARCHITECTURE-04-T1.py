weights = {"fiabilite": 3, "cout": 1, "ecosysteme": 2}
options = {
    "PostgreSQL": {"maitrise": 4, "fiabilite": 5, "cout": 4, "ecosysteme": 5},
    "MongoDB": {"maitrise": 3, "fiabilite": 3, "cout": 3, "ecosysteme": 4},
    "Cassandra": {"maitrise": 1, "fiabilite": 5, "cout": 2, "ecosysteme": 3},
}


def ranking(current_weights):
    kept = {name: notes for name, notes in options.items() if notes["maitrise"] >= 3}
    scored = {name: sum(notes[criterion] * weight for criterion, weight in current_weights.items()) for name, notes in kept.items()}
    return sorted(scored.items(), key=lambda item: (-item[1], item[0]))


print("Ecartees: " + ", ".join(name for name, notes in options.items() if notes["maitrise"] < 3))
base = ranking(weights)
for position, (name, score) in enumerate(base, start=1):
    print(f"{position}. {name}: {score}")
print(f"Choix: {base[0][0]}")
heavier = ranking({**weights, "cout": weights["cout"] * 2})
print("Sensibilite: " + (f"le choix devient {heavier[0][0]}" if heavier[0][0] != base[0][0] else "le choix ne change pas"))
