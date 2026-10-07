query = {"java", "jvm"}
documents = {"A": {"java", "bytecode", "jvm"}, "B": {"python", "interpréteur"}, "C": {"java", "spring"}}
ranked = sorted(documents, key=lambda key: len(query & documents[key]), reverse=True)
print("Classement:", " > ".join(ranked))
print("Premier score:", len(query & documents[ranked[0]]))
