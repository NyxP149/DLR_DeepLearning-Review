import re

CORPUS = {
    "PIP": "pip installe les paquets python depuis pypi",
    "VENV": "venv cree un environnement virtuel isole",
    "PYTEST": "pytest execute les tests unitaires",
    "FLAKE": "ruff analyse le style du code",
}
CASES = [
    ("Qui installe les paquets ?", "PIP"),
    ("Que cree venv ?", "VENV"),
    ("Qui execute les tests ?", "PYTEST"),
    ("Qui analyse le style ?", "FLAKE"),
    ("Quelle est la meteo ?", None),
    ("Qui a gagne la coupe ?", None),
]


def words(text):
    return set(re.findall(r"\w+", text.lower()))


def search(question):
    best = max(CORPUS.items(), key=lambda item: len(words(question) & words(item[1])))
    return best[0] if len(words(question) & words(best[1])) >= 2 else None


good_sources = justified = undue = answerable = unanswerable = 0
for question, expected in CASES:
    source = search(question)
    if expected is None:
        unanswerable += 1
        justified += source is None
        print(f"{question} -> {'REFUS' if source is None else source} [{'OK' if source is None else 'ERREUR'}]")
    else:
        answerable += 1
        if source is None:
            undue += 1
            print(f"{question} -> REFUS [ERREUR]")
        else:
            good_sources += source == expected
            print(f"{question} -> {source} [{'OK' if source == expected else 'ERREUR'}]")
print(f"Sources exactes: {good_sources}/{answerable}")
print(f"Refus justifies: {justified}/{unanswerable}")
print(f"Refus indus: {undue}")
