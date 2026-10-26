need = "Annuler une reservation"
contract = {"method": "DELETE", "path": "/bookings/{id}", "success": 204, "errors": [401, 404, 409]}
criteria = {
    "reservation active annulee": 204,
    "reservation inconnue refusee": 404,
    "reservation deja consommee refusee": 409,
    "annulation sans droit refusee": 403,
}

statuses = [contract["success"]] + contract["errors"]
print(f"Besoin: {need}")
print(f"Contrat: {contract['method']} {contract['path']} -> {contract['success']}")
for criterion, status in criteria.items():
    print(f"{criterion}: " + (f"couvert ({status})" if status in statuses else "NON COUVERT"))
orphans = [status for status in statuses if status not in criteria.values()]
print("Statuts sans critere: " + (", ".join(map(str, orphans)) if orphans else "aucun"))
