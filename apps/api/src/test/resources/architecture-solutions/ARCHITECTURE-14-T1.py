users = {"lea": "employee", "marc": "manager", "dina": "director"}
requests = {"d1": {"author": "lea", "amount": 200}, "d2": {"author": "lea", "amount": 800}}
attempts = [("lea", "d1"), ("marc", "d2"), ("dina", "d2"), ("marc", "d1")]

tick = [100]


def clock():
    value = tick[0]
    tick[0] += 10
    return value


audit = []


def approve(user, request_id):
    request = requests[request_id]
    role = users[user]
    if role not in ("manager", "director"):
        reason = "role insuffisant"
    elif request["author"] == user:
        reason = "auteur de la demande"
    elif request["amount"] > 500 and role != "director":
        reason = "seuil reserve au director"
    else:
        reason = None
    audit.append((clock(), request_id, user, "DENIED" if reason else "APPROVED", reason or "ok"))
    return 403 if reason else 200


for user, request_id in attempts:
    print(f"{user} approuve {request_id} -> {approve(user, request_id)}")
print("Audit:")
for entry in audit:
    print("  " + " ".join(map(str, entry)))
