def prompt(task: str, data: str) -> str:
    return f"TÂCHE: {task}\nDONNÉES:\n<data>{data}</data>\nFORMAT: JSON"

value = prompt("résumer", "Java utilise la JVM")
print(value)
