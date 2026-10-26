STORY = "Créer une commande"
CONTRACT = {
    "method": "POST",
    "path": "/orders",
    "success": 201,
    "errors": {400: "commande vide", 404: "client inconnu", 409: "clé rejouée avec un autre contenu"},
}
CRITERIA = {
    "commande valide créée": 201,
    "commande vide refusée": 400,
    "client inconnu refusé": 404,
    "livraison express gratuite": 201,
    "paiement refusé": 402,
}

print(f"Besoin: {STORY}")
print(f"Contrat: {CONTRACT['method']} {CONTRACT['path']} -> {CONTRACT['success']}")
statuses = {CONTRACT["success"], *CONTRACT["errors"]}
covered = 0
for criterion, status in CRITERIA.items():
    ok = status in statuses
    covered += ok
    print(f"{criterion}: {'couvert' if ok else 'NON COUVERT'} ({status})")
unused = sorted(statuses - set(CRITERIA.values()))
print("Statuts sans critère: " + (", ".join(str(s) for s in unused) or "aucun"))
print(f"Couverture: {covered}/{len(CRITERIA)}")
