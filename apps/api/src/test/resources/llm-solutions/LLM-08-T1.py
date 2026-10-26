docs = {
    "D1": "python liste dictionnaire",
    "D2": "java jvm bytecode",
    "D3": "sql jointure index",
    "D4": "python async await",
}
queries = [
    ("liste python", "D1"),
    ("index sql", "D3"),
    ("bytecode python", "D2"),
]


def ranking(query):
    words = set(query.split())
    scored = [(doc_id, len(words & set(text.split()))) for doc_id, text in docs.items()]
    return sorted(scored, key=lambda item: (-item[1], item[0]))


def recall_at(k):
    return sum(1 for query, relevant in queries if relevant in [doc for doc, _ in ranking(query)[:k]]) / len(queries)


def mrr():
    total = 0.0
    for query, relevant in queries:
        order = [doc for doc, _ in ranking(query)]
        total += 1 / (order.index(relevant) + 1)
    return total / len(queries)


first = ranking(queries[0][0])
print("Classement: " + " > ".join(doc for doc, _ in first))
print(f"Premier score: {first[0][1]}")
print(f"recall@1: {recall_at(1):.2f}")
print(f"recall@2: {recall_at(2):.2f}")
print(f"MRR: {mrr():.3f}")
