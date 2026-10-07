scores = {"clair": 0.55, "précis": 0.30, "créatif": 0.15}
ranked = sorted(scores.items(), key=lambda item: item[1], reverse=True)
print("Choix déterministe:", ranked[0][0])
print("Top-2:", ", ".join(word for word, _ in ranked[:2]))
