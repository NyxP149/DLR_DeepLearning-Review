STRATEGIES = {
    "contexte_complet": {"quality": 0.90, "tokens": 900, "citations": 1},
    "rag_ciblé": {"quality": 0.88, "tokens": 240, "citations": 3},
}
PROFILES = {
    "défaut": {"citations": 0.05, "tokens": 1 / 5000},
    "qualité d'abord": {"citations": 0.0, "tokens": 0.0},
    "coût d'abord": {"citations": 0.0, "tokens": 1 / 1000},
}


def score(strategy: dict, weights: dict) -> float:
    return strategy["quality"] + strategy["citations"] * weights["citations"] - strategy["tokens"] * weights["tokens"]


def best(weights: dict) -> str:
    return max(sorted(STRATEGIES), key=lambda name: score(STRATEGIES[name], weights))


default = PROFILES["défaut"]
for name, strategy in STRATEGIES.items():
    print(f"{name}: score {score(strategy, default):.3f}")
chosen = best(default)
reduction = round(100 * (1 - STRATEGIES[chosen]["tokens"] / STRATEGIES["contexte_complet"]["tokens"]))
print(f"Stratégie retenue: {chosen}")
print(f"Réduction tokens: {reduction}%")
winners = {}
for profile, weights in PROFILES.items():
    winners[profile] = best(weights)
    print(f"{profile} -> {winners[profile]}")
print(f"Décision robuste: {len(set(winners.values())) == 1}")
