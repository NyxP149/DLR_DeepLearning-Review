import json
import re

responses = [
    '{"label": "positif", "score": 9}',
    '```json\n{"label": "neutre", "score": 5}\n```',
    'pas du json',
    '{"label": "positif", "score": 15}',
    '{"label": "negatif", "score": 2, "extra": true}',
]


def validate(text):
    cleaned = re.sub(r"^```(?:json)?\s*|\s*```$", "", text.strip())
    try:
        data = json.loads(cleaned)
    except json.JSONDecodeError:
        return "rejete: JSON invalide"
    if not isinstance(data, dict) or set(data) != {"label", "score"}:
        return "rejete: cles inattendues"
    if data["label"] not in ("positif", "negatif", "neutre"):
        return "rejete: label inconnu"
    score = data["score"]
    if isinstance(score, bool) or not isinstance(score, int) or not 0 <= score <= 10:
        return "rejete: score hors de [0, 10]"
    return f"accepte: {data['label']} ({score})"


for index, response in enumerate(responses, start=1):
    print(f"{index}. {validate(response)}")
