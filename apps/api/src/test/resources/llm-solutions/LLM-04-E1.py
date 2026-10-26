import json

SAMPLES = [
    '{"answer":"JVM","confidence":0.92}',
    '```json\n{"answer":"JDK","confidence":0.5}\n```',
    'Voici la réponse : {"answer":"JVM"}',
    '{"answer":"JVM","confidence":1.7}',
    '{"answer":"JVM","confidence":0.9,"extra":1}',
]


def validate(raw: str) -> dict:
    text = raw.strip()
    if text.startswith("```"):
        text = text.strip("`").removeprefix("json").strip()
    try:
        data = json.loads(text)
    except json.JSONDecodeError:
        raise ValueError("JSON invalide") from None
    if not isinstance(data, dict) or set(data) != {"answer", "confidence"}:
        raise ValueError("clés inattendues")
    answer, confidence = data["answer"], data["confidence"]
    if not isinstance(answer, str) or not answer.strip():
        raise ValueError("answer vide")
    if isinstance(confidence, bool) or not isinstance(confidence, (int, float)) or not 0 <= confidence <= 1:
        raise ValueError("confidence hors de [0, 1]")
    return data


for index, raw in enumerate(SAMPLES, start=1):
    try:
        data = validate(raw)
        print(f"{index}. accepté: {data['answer']} ({round(data['confidence'] * 100)}%)")
    except ValueError as error:
        print(f"{index}. rejeté: {error}")
