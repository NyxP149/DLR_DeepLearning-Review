documents = {"JVM": "La JVM exécute le bytecode.", "JDK": "Le JDK contient les outils de développement."}
question = "Qui exécute le bytecode ?"
source = next(key for key, text in documents.items() if "bytecode" in text)
answer = f"{documents[source]} [source:{source}]"
print(question)
print(answer)
