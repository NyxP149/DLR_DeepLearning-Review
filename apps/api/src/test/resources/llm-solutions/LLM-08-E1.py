DOCUMENTS = {
    "A": {"java", "bytecode", "jvm"},
    "B": {"python", "interpréteur"},
    "C": {"java", "spring"},
    "D": {"jvm", "garbage", "collector"},
}
QUERIES = [
    ({"java", "jvm"}, "A"),
    ({"spring"}, "C"),
    ({"garbage", "java"}, "D"),
]


def rank(query: set) -> list[str]:
    return sorted(DOCUMENTS, key=lambda doc: (-len(query & DOCUMENTS[doc]), doc))


def reciprocal_rank(ranking: list[str], relevant: str) -> float:
    for position, doc in enumerate(ranking, start=1):
        if doc == relevant:
            return 1 / position
    return 0.0


first_query = QUERIES[0][0]
ranking = rank(first_query)
print("Classement: " + " > ".join(ranking))
print(f"Premier score: {len(first_query & DOCUMENTS[ranking[0]])}")
rankings = [(rank(query), relevant) for query, relevant in QUERIES]
for k in (1, 2):
    recall = sum(1 for ranking_, relevant in rankings if relevant in ranking_[:k]) / len(rankings)
    print(f"recall@{k}: {recall:.2f}")
mrr = sum(reciprocal_rank(ranking_, relevant) for ranking_, relevant in rankings) / len(rankings)
print(f"MRR: {mrr:.3f}")
