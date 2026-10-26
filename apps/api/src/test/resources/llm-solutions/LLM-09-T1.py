import re

CORPUS = {
    "GIL": "Le GIL limite l'execution de plusieurs threads Python en parallele.",
    "VENV": "Un venv isole les dependances d'un projet Python.",
}
questions = ["Que limite le GIL en Python ?", "Quelle est la capitale de la France ?"]


def words(text):
    return set(re.findall(r"\w+", text.lower()))


def answer(question):
    best = max(CORPUS.items(), key=lambda item: len(words(question) & words(item[1])))
    if len(words(question) & words(best[1])) < 2:
        return "Je ne sais pas."
    return f"{best[1]} [source:{best[0]}]"


for question in questions:
    reply = answer(question)
    print(f"Q: {question}")
    print(f"R: {reply}")
    cited = re.findall(r"\[source:(\w+)\]", reply)
    print(f"Citations valides: {all(source in CORPUS for source in cited)}")
