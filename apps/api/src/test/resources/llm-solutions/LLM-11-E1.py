import re
from dataclasses import dataclass

CORPUS = {
    "JVM": "La JVM exécute le bytecode Java.",
    "JDK": "Le JDK contient le compilateur et les outils.",
    "GC": "Le ramasse-miettes libère la mémoire inutilisée.",
    "Maven": "Maven gère les dépendances et le cycle de build.",
}
STOP_WORDS = {"qui", "que", "quoi", "le", "la", "les", "de", "des", "un", "une", "est", "ce", "du", "quel", "quelle", "où", "et"}
CASES = [
    ("Qui exécute le bytecode ?", "JVM"),
    ("Que contient le JDK ?", "JDK"),
    ("Que libère le ramasse-miettes ?", "GC"),
    ("Qui gère les dépendances ?", "Maven"),
    ("Quelle est la météo ?", None),
    ("Qui a gagné le match ?", None),
]


@dataclass(frozen=True)
class Answer:
    text: str
    source: str | None


def keywords(text: str) -> set[str]:
    return {word for word in re.findall(r"[\w-]+", text.lower()) if word not in STOP_WORDS}


def assistant(question: str) -> Answer:
    asked = keywords(question)
    scored = sorted(((-len(asked & keywords(text)), key) for key, text in CORPUS.items()))
    best_score, best_key = scored[0]
    if best_score == 0:
        return Answer("Je ne sais pas", None)
    return Answer(CORPUS[best_key], best_key)


exact = unjustified = justified = unanswerable = 0
for question, expected in CASES:
    reply = assistant(question)
    ok = reply.source == expected
    exact += ok
    if expected is None:
        unanswerable += 1
        justified += reply.source is None
    elif reply.source is None:
        unjustified += 1
    print(f"{question} -> {reply.source or 'refus'}  [{'OK' if ok else 'ÉCHEC'}]")
print(f"Sources exactes: {exact}/{len(CASES)}")
print(f"Refus justifiés: {justified}/{unanswerable}")
print(f"Refus indus: {unjustified}")
