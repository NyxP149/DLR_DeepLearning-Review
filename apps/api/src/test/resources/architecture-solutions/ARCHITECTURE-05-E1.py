order = {"id": 42, "customer_id": 7, "lines": [25, 15], "status": "DRAFT"}
assert order["lines"] and all(price > 0 for price in order["lines"])
print("Total:", sum(order["lines"]))
print("Invariant: valide")
