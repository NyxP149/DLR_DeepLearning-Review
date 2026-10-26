import math

EMBEDDINGS = {
    "voiture": [1.0, 0.1],
    "automobile": [0.95, 0.15],
    "banane": [0.0, 1.0],
}


def cosine(a, b):
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = math.sqrt(sum(x * x for x in a))
    norm_b = math.sqrt(sum(y * y for y in b))
    return 0.0 if norm_a == 0 or norm_b == 0 else dot / (norm_a * norm_b)


def bag(word, vocabulary):
    return [1.0 if word == candidate else 0.0 for candidate in vocabulary]


vocabulary = list(EMBEDDINGS)
print(f"Sac de mots voiture/automobile: {cosine(bag('voiture', vocabulary), bag('automobile', vocabulary)):.3f}")
print(f"Embedding voiture/automobile: {cosine(EMBEDDINGS['voiture'], EMBEDDINGS['automobile']):.3f}")
ranking = sorted(
    ((word, cosine(EMBEDDINGS['voiture'], vector)) for word, vector in EMBEDDINGS.items() if word != 'voiture'),
    key=lambda item: -item[1],
)
print("Classement pour voiture: " + " ".join(f"{word}={score:.3f}" for word, score in ranking))
