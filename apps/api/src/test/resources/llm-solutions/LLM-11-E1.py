from dataclasses import dataclass

@dataclass(frozen=True)
class Answer:
    text: str
    source: str

corpus = {"JVM": "La JVM exécute le bytecode", "JDK": "Le JDK compile le code"}
answer = Answer(corpus["JVM"], "JVM")
assert "bytecode" in answer.text and answer.source in corpus
print(f"Réponse: {answer.text} [{answer.source}]")
print("Évaluation: 2/2")
