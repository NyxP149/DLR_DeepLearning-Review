trace = {"id": "abc-42", "spans": [18, 42, 25], "errors": 0}
print("Trace:", trace["id"])
print("Durée:", sum(trace["spans"]), "ms")
print("Statut:", "OK" if trace["errors"] == 0 else "ERROR")
