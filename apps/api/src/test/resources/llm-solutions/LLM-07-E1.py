def chunks(words: list[str], size: int, overlap: int) -> list[list[str]]:
    step = size - overlap
    return [words[i:i+size] for i in range(0, len(words), step) if words[i:i+size]]

parts = chunks("un deux trois quatre cinq six".split(), 3, 1)
print(" | ".join(" ".join(part) for part in parts))
