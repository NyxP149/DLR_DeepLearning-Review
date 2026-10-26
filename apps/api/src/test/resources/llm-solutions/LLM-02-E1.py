import math

logits = {"clair": 2.0, "précis": 1.0, "créatif": 0.0}


def softmax(scores: dict, temperature: float = 1.0) -> dict:
    exps = {word: math.exp(score / temperature) for word, score in scores.items()}
    total = sum(exps.values())
    return {word: value / total for word, value in exps.items()}


def ranked(probs: dict) -> list:
    return sorted(probs.items(), key=lambda item: (-item[1], item[0]))


def top_k(probs: dict, k: int) -> dict:
    best = dict(ranked(probs)[:k])
    total = sum(best.values())
    return {word: p / total for word, p in best.items()}


def pick(probs: dict, u: float) -> str:
    cumulative = 0.0
    for word, p in ranked(probs):
        cumulative += p
        if cumulative >= u:
            return word
    return ranked(probs)[-1][0]


def show(label: str, probs: dict) -> None:
    print(label + " " + " ".join(f"{word}={p:.3f}" for word, p in probs.items()))


show("T=1.0:", softmax(logits, 1.0))
show("T=0.5:", softmax(logits, 0.5))
show("Top-2 (T=1.0):", top_k(softmax(logits, 1.0), 2))
base = softmax(logits, 1.0)
print("Tirages u=0.10/0.80/0.95 (T=1.0): " + ", ".join(pick(base, u) for u in (0.10, 0.80, 0.95)))
print("Greedy: " + max(logits, key=logits.get))
