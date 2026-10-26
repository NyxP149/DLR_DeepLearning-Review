products = [
    ("Clavier", "bureau", 49.9, True),
    ("Casque", "audio", 59.0, True),
    ("Cable", "bureau", 4.5, True),
    ("Souris", "bureau", 19.9, False),
]
in_stock = sorted(name for name, _, _, stock in products if stock)
categories = sorted({category for _, category, _, _ in products})
counts = {}
for _, category, _, _ in products:
    counts[category] = counts.get(category, 0) + 1
cheapest = min(products, key=lambda product: product[2])
print("En stock:", ", ".join(in_stock))
print("Categories:", categories)
print("Par categorie:", dict(sorted(counts.items())))
print(f"Moins cher: {cheapest[0]} ({cheapest[2]})")
print("Source intacte:", len(products))
