import math

logits = {"rouge": 2.0, "vert": 1.0, "bleu": 0.1}


def softmax(values, temperature):
    exps = {name: math.exp(value / temperature) for name, value in values.items()}
    total = sum(exps.values())
    return {name: value / total for name, value in exps.items()}


def top_k(probabilities, k):
    best = dict(sorted(probabilities.items(), key=lambda item: -item[1])[:k])
    total = sum(best.values())
    return {name: value / total for name, value in best.items()}


def draw(probabilities, u):
    cumulative = 0.0
    for name, value in probabilities.items():
        cumulative += value
        if u < cumulative:
            return name
    return name


def show(probabilities):
    return " ".join(f"{name}={value:.3f}" for name, value in probabilities.items())


print(f"T=1.0: {show(softmax(logits, 1.0))}")
print(f"T=0.5: {show(softmax(logits, 0.5))}")
print(f"Top-2 (T=1.0): {show(top_k(softmax(logits, 1.0), 2))}")
base = softmax(logits, 1.0)
print("Tirages: " + " ".join(f"u={u:.2f}->{draw(base, u)}" for u in (0.10, 0.80, 0.95)))
