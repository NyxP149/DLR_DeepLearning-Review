import re

DOCUMENTS = {
    "JVM": "La JVM exécute le bytecode.",
    "JDK": "Le JDK contient les outils de développement.",
    "GC": "Le ramasse-miettes libère la mémoire inutilisée.",
}
STOP_WORDS = {"qui", "le", "la", "les", "de", "des", "un", "une", "est", "que", "quoi", "ce", "du"}
MIN_SHARED = 1


def keywords(text: str) -> set[str]:
    words = re.findall(r"[\w-]+", text.lower())
    return {word for word in words if word not in STOP_WORDS}


def retrieve(question: str) -> list[tuple[str, str]]:
    asked = keywords(question)
    scored = []
    for key, text in DOCUMENTS.items():
        shared = len(asked & keywords(text))
        if shared >= MIN_SHARED:
            scored.append((-shared, key, text))
    return [(key, text) for _, key, text in sorted(scored)]


def answer(question: str) -> str:
    hits = retrieve(question)
    if not hits:
        return "Je ne sais pas"
    key, text = hits[0]
    return f"{text} [source:{key}]"


def citations_valid(text: str) -> bool:
    return all(cited in DOCUMENTS for cited in re.findall(r"\[source:([^\]]+)\]", text))


for question in ["Qui exécute le bytecode ?", "Que libère le ramasse-miettes ?", "Quelle est la météo ?"]:
    reply = answer(question)
    print(f"Q: {question}")
    print(f"R: {reply}")
    print(f"Citations valides: {citations_valid(reply)}")

print(f"Citation inventée détectée: {not citations_valid('La JVM exécute le bytecode. [source:XYZ]')}")
