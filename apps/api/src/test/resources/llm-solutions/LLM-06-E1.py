from math import sqrt

def cosine(a: list[float], b: list[float]) -> float:
    dot = sum(x * y for x, y in zip(a, b))
    return dot / (sqrt(sum(x*x for x in a)) * sqrt(sum(y*y for y in b)))

print(f"Similarité: {cosine([1, 1, 0], [1, 0.5, 0]):.3f}")
