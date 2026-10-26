TRACE = "xyz-77"
spans = [
    {"name": "api", "parent": None, "ms": 200, "error": False},
    {"name": "auth", "parent": "api", "ms": 30, "error": False},
    {"name": "orders", "parent": "api", "ms": 150, "error": False},
    {"name": "db", "parent": "orders", "ms": 40, "error": False},
    {"name": "mailer", "parent": "orders", "ms": 90, "error": True},
]

children = {span["name"]: [c for c in spans if c["parent"] == span["name"]] for span in spans}
own = {span["name"]: span["ms"] - sum(c["ms"] for c in children[span["name"]]) for span in spans}


def show(span, depth):
    print(f"{'  ' * depth}{span['name']} {span['ms']} ms (propre {own[span['name']]} ms)")
    for child in children[span["name"]]:
        show(child, depth + 1)


print(f"Trace: {TRACE}")
show(next(span for span in spans if span["parent"] is None), 0)
slowest = max(spans, key=lambda span: own[span["name"]])
print(f"Plus grand temps propre: {slowest['name']} ({own[slowest['name']]} ms)")
print("Spans en erreur: " + ", ".join(span["name"] for span in spans if span["error"]))
