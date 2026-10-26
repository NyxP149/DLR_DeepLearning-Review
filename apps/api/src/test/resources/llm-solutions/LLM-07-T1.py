lines = ["l0", "l1", "l2", "l3", "l4", "l5"]


def chunks(source, items, size, overlap):
    if overlap >= size:
        raise ValueError("size doit depasser overlap")
    step = size - overlap
    result = []
    start = 0
    while start < len(items):
        window = items[start:start + size]
        result.append({"source": source, "start": start, "text": " / ".join(window)})
        if start + size >= len(items):
            break
        start += step
    return result


pieces = chunks("doc-9", lines, 3, 1)
for piece in pieces:
    print(f"{piece['source']}@{piece['start']}: {piece['text']}")
covered = {line for piece in pieces for line in piece["text"].split(" / ")}
print("Couverture complete:", covered == set(lines))
try:
    chunks("doc-9", lines, 2, 2)
except ValueError as error:
    print(f"Refuse: {error}")
