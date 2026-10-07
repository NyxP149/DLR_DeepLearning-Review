events = ["order:42", "order:42", "order:43"]
processed = set()
for event in events:
    if event in processed:
        print(event, "IGNORÉ")
    else:
        processed.add(event)
        print(event, "TRAITÉ")
