messages = [
    ("pay:1", 0),
    ("pay:1", 0),
    ("pay:2", 2),
    ("pay:3", 9),
    ("pay:4", 1),
]
done = set()
dead_letters = []

for message_id, failures_before_success in messages:
    if message_id in done:
        print(f"{message_id}: IGNORE (doublon)")
        continue
    for attempt in range(1, 4):
        if attempt > failures_before_success:
            done.add(message_id)
            print(f"{message_id}: TRAITE apres {attempt} tentative(s)")
            break
    else:
        dead_letters.append(message_id)
        print(f"{message_id}: DLQ apres 3 tentatives")
print(f"File d'echecs: {dead_letters}")
