MAX_ATTEMPTS = 3
EVENTS = [
    {"id": "order:42", "kind": "ok"},
    {"id": "order:42", "kind": "ok"},
    {"id": "order:43", "kind": "flaky"},
    {"id": "order:44", "kind": "poison"},
    {"id": "order:45", "kind": "ok"},
]
processed: set[str] = set()
dlq: list[dict] = []
failures_seen: dict[str, int] = {}


def process(event: dict) -> None:
    kind = event["kind"]
    failures_seen[event["id"]] = failures_seen.get(event["id"], 0) + 1
    if kind == "poison" or (kind == "flaky" and failures_seen[event["id"]] <= 2):
        raise RuntimeError("erreur transitoire")


def handle(event: dict) -> str:
    if event["id"] in processed:
        return "IGNORÉ (doublon)"
    for attempt in range(1, MAX_ATTEMPTS + 1):
        try:
            process(event)
        except RuntimeError:
            continue
        processed.add(event["id"])
        return f"TRAITÉ après {attempt} tentative(s)"
    dlq.append(event)
    return f"DLQ après {MAX_ATTEMPTS} tentatives"


for event in EVENTS:
    print(f"{event['id']}: {handle(event)}")
print(f"Traités: {len(processed)}")
print("File d'échecs: " + ", ".join(item["id"] for item in dlq))
