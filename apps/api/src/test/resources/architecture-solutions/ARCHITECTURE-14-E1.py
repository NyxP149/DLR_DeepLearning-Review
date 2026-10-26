REQUESTS = {
    "r1": {"author": "eva", "amount": 400},
    "r2": {"author": "eva", "amount": 1500},
    "r3": {"author": "marc", "amount": 250},
}
USERS = {
    "eva": "employee",
    "marc": "manager",
    "dina": "director",
}
APPROVERS = {"manager", "director"}
THRESHOLD = 1000
audit: list[tuple] = []
clock = iter(range(100, 200, 10))


def approve(user: str, request_id: str) -> int:
    request = REQUESTS[request_id]
    role = USERS[user]
    reason = None
    if role not in APPROVERS:
        reason = "rôle insuffisant"
    elif user == request["author"]:
        reason = "auto-approbation interdite"
    elif request["amount"] > THRESHOLD and role != "director":
        reason = "seuil dépassé"
    audit.append((next(clock), request_id, user, "DENIED" if reason else "APPROVED", reason or "-"))
    return 403 if reason else 200


attempts = [("eva", "r1"), ("marc", "r2"), ("dina", "r2"), ("marc", "r3"), ("marc", "r1")]
for user, request_id in attempts:
    print(f"{user} approuve {request_id} -> {approve(user, request_id)}")
print("Audit:")
for instant, request_id, user, decision, reason in audit:
    print(f"  {instant} {request_id} {user} {decision} {reason}")
