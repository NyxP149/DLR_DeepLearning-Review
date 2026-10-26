STRATEGIES = {
    "contexte_complet": {"qualite": 0.90, "cout": 0.30, "tracabilite": 0.40, "tokens": 8000},
    "rag_cible": {"qualite": 0.85, "cout": 0.90, "tracabilite": 0.95, "tokens": 2000},
    "resume": {"qualite": 0.60, "cout": 0.95, "tracabilite": 0.20, "tokens": 1000},
}
PROFILS = {
    "defaut": {"qualite": 0.5, "cout": 0.3, "tracabilite": 0.2},
    "qualite d'abord": {"qualite": 1.0, "cout": 0.0, "tracabilite": 0.0},
    "economie": {"qualite": 0.2, "cout": 0.7, "tracabilite": 0.1},
}


def score(strategy, weights):
    return sum(STRATEGIES[strategy][criterion] * weight for criterion, weight in weights.items())


def best(weights):
    return max(STRATEGIES, key=lambda name: score(name, weights))


for name in STRATEGIES:
    print(f"{name}: score {score(name, PROFILS['defaut']):.3f}")
chosen = best(PROFILS["defaut"])
print(f"Strategie retenue: {chosen}")
most = max(strategy["tokens"] for strategy in STRATEGIES.values())
print(f"Reduction tokens: {round(100 * (most - STRATEGIES[chosen]['tokens']) / most)}%")
choices = {profile: best(weights) for profile, weights in PROFILS.items()}
for profile, choice in choices.items():
    print(f"{profile} -> {choice}")
print("Verdict: decision robuste" if len(set(choices.values())) == 1 else "Verdict: decision sensible aux poids")
