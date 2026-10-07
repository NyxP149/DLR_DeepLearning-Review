story = "Créer une commande"
contract = {"method": "POST", "path": "/orders", "success": 201}
print("Besoin:", story)
print(f"Contrat: {contract['method']} {contract['path']} -> {contract['success']}")
