suite = {"unitaires": 42, "intégration": 12, "contrats": 6, "e2e": 3}
print("Tests:", sum(suite.values()))
print("Couverture:", " -> ".join(suite))
