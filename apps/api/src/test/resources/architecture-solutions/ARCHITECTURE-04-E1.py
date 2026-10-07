stacks = {
    "Spring-Angular": {"team": 5, "speed": 3, "ops": 4},
    "FastAPI-TypeScript": {"team": 3, "speed": 5, "ops": 3},
}
weights = {"team": 2, "speed": 1, "ops": 2}
scores = {name: sum(values[key] * weights[key] for key in weights) for name, values in stacks.items()}
winner = max(scores, key=scores.get)
print("Choix:", winner)
print("Score:", scores[winner])
