def chunks(source: str, words: list[str], size: int, overlap: int) -> list[dict]:
    if size <= overlap:
        raise ValueError("size doit dépasser overlap")
    step = size - overlap
    result = []
    for start in range(0, len(words), step):
        part = words[start:start + size]
        if part:
            result.append({"source": source, "start": start, "text": " ".join(part)})
        if start + size >= len(words):
            break
    return result


words = "un deux trois quatre cinq six".split()
parts = chunks("doc-1", words, 3, 1)
print(" | ".join(part["text"] for part in parts))
for part in parts:
    print(f"{part['source']}@{part['start']}: {part['text']}")
covered = {word for part in parts for word in part["text"].split()}
print(f"Couverture complète: {covered == set(words)}")
try:
    chunks("doc-1", words, 2, 2)
except ValueError as error:
    print(f"Refusé: {error}")
print(f"Court texte: {len(chunks('doc-2', ['a', 'b'], 5, 1))}")
