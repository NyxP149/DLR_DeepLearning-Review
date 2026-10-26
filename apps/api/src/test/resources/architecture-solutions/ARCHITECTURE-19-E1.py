SPANS = [
    ("s1", None, "gateway", 0, 120, False),
    ("s2", "s1", "orders", 5, 115, False),
    ("s3", "s2", "db", 10, 30, False),
    ("s4", "s2", "payment", 35, 110, True),
]
TRACE_ID = "abc-42"


def duration(span: tuple) -> int:
    return span[4] - span[3]


def children_of(span_id: str) -> list[tuple]:
    return [span for span in SPANS if span[1] == span_id]


def self_time(span: tuple) -> int:
    return duration(span) - sum(duration(child) for child in children_of(span[0]))


def render(span: tuple, depth: int = 0) -> None:
    print(f"{'  ' * depth}{span[2]} {duration(span)} ms (propre {self_time(span)} ms)")
    for child in children_of(span[0]):
        render(child, depth + 1)


print(f"Trace: {TRACE_ID}")
root = next(span for span in SPANS if span[1] is None)
render(root)
slowest = max(SPANS, key=self_time)
print(f"Plus grand temps propre: {slowest[2]} ({self_time(slowest)} ms)")
errors = [span[2] for span in SPANS if span[5]]
print("Spans en erreur: " + (", ".join(errors) or "aucun"))
print("Statut: " + ("ERROR" if errors else "OK"))
