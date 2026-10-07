options = {
    "monolithe_modulaire": {"delivery": 5, "reliability": 4, "cost": 5},
    "microservices": {"delivery": 2, "reliability": 3, "cost": 2},
}
weights = {"delivery": 3, "reliability": 2, "cost": 2}
scores = {name: sum(values[key] * weights[key] for key in weights) for name, values in options.items()}
choice = max(scores, key=scores.get)
print("Décision:", choice)
print("Score:", scores[choice])
print("Condition de réévaluation: limites mesurées")
