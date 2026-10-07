attempts = [False, False, True]
for number, success in enumerate(attempts, 1):
    print(f"Tentative {number}: {'OK' if success else 'RETRY'}")
    if success:
        break
print("RPO: 5 min | RTO: 30 min")
