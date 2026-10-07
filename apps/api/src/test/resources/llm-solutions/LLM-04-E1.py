import json

def validate(raw: str) -> dict:
    value = json.loads(raw)
    if set(value) != {"answer", "confidence"} or not 0 <= value["confidence"] <= 1:
        raise ValueError("réponse invalide")
    return value

result = validate('{"answer":"JVM","confidence":0.92}')
print(f"{result['answer']} ({result['confidence']:.0%})")
