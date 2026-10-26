options = {
    "monolithe": {"delai": 8, "budget": 1500, "simplicite": 5, "scalabilite": 2, "cout": 5},
    "microservices": {"delai": 20, "budget": 6000, "simplicite": 2, "scalabilite": 5, "cout": 2},
    "serverless": {"delai": 10, "budget": 2500, "simplicite": 3, "scalabilite": 4, "cout": 3},
}
weights = {"simplicite": 3, "scalabilite": 2, "cout": 1}
context = "equipe de 3, lancement dans 3 mois"
reevaluation = ["plus de 5 000 requetes par seconde", "equipe de plus de 10 personnes"]

kept = {}
discarded = []
for name, notes in options.items():
    violated = [label for label, failed in (("delai", notes["delai"] > 12), ("budget", notes["budget"] > 3000)) if failed]
    if violated:
        print(f"{name}: ecartee ({', '.join(violated)})")
        discarded.append(name)
    else:
        kept[name] = sum(notes[criterion] * weight for criterion, weight in weights.items())
        print(f"{name}: score {kept[name]}")
choice = max(kept, key=kept.get)
print(f"Decision: {choice} (score {kept[choice]})")
print("ADR:")
print(f"  Contexte: {context}")
print(f"  Decision: {choice}")
print(f"  Options ecartees: {', '.join(discarded)}")
for condition in reevaluation:
    print(f"  Reevaluer si: {condition}")
