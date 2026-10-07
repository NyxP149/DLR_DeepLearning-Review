strategies = {
    "contexte_complet": {"quality": 0.90, "tokens": 900, "citations": 1},
    "rag_ciblé": {"quality": 0.88, "tokens": 240, "citations": 3},
}
for values in strategies.values():
    values["score"] = values["quality"] + values["citations"] * 0.05 - values["tokens"] / 5000
winner = max(strategies, key=lambda name: strategies[name]["score"])
print("Stratégie retenue:", winner)
print("Réduction tokens: 73%")
