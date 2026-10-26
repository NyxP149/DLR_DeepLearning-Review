from math import sqrt

DOCUMENTS = {
    "jvm": "la jvm exécute le bytecode java",
    "jdk": "le jdk contient le compilateur javac",
    "python": "python est interprété",
}


def bag_of_words(text: str, vocabulary: list[str]) -> list[float]:
    words = text.split()
    return [float(words.count(word)) for word in vocabulary]


def cosine(a: list[float], b: list[float]) -> float:
    dot = sum(x * y for x, y in zip(a, b))
    norm = sqrt(sum(x * x for x in a)) * sqrt(sum(y * y for y in b))
    return dot / norm if norm else 0.0


vocabulary = sorted({word for text in DOCUMENTS.values() for word in text.split()} | {"voiture", "automobile"})
query = "jvm exécute bytecode"
query_vector = bag_of_words(query, vocabulary)
scores = {name: cosine(query_vector, bag_of_words(text, vocabulary)) for name, text in DOCUMENTS.items()}
ranking = sorted(scores.items(), key=lambda item: (-item[1], item[0]))
print("Classement: " + " ".join(f"{name}={score:.3f}" for name, score in ranking))
synonyms = cosine(bag_of_words("voiture", vocabulary), bag_of_words("automobile", vocabulary))
print(f"Voiture vs automobile: {synonyms:.3f}")
