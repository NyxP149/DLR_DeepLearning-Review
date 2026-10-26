BASE, CAP = 1.0, 8.0
FAILURE_THRESHOLD, RESET_AFTER = 3, 30
BACKUP_INTERVAL_MIN, MEASURED_RESTORE_MIN = 15, 30
RPO_MIN, RTO_MIN = 5, 45


def backoff(attempt: int) -> float:
    return min(CAP, BASE * 2 ** (attempt - 1))


class Breaker:
    def __init__(self) -> None:
        self.state = "closed"
        self.failures = 0
        self.opened_at = 0

    def call(self, now: int, succeeds: bool) -> str:
        if self.state == "open" and now - self.opened_at >= RESET_AFTER:
            self.state = "half-open"
        if self.state == "open":
            return "REFUSÉ"
        if succeeds:
            self.failures = 0
            self.state = "closed"
            return "OK"
        self.failures += 1
        if self.state == "half-open" or self.failures >= FAILURE_THRESHOLD:
            self.state = "open"
            self.opened_at = now
        return "ÉCHEC"


print("Délais de réessai: " + ", ".join(f"{backoff(n):.1f}" for n in range(1, 6)))
breaker = Breaker()
timeline = [(0, False), (1, False), (2, False), (3, True), (31, True), (32, False), (33, True)]
for now, succeeds in timeline:
    result = breaker.call(now, succeeds)
    print(f"t={now}: {result} (état {breaker.state})")
print(f"RPO {RPO_MIN} min: {'respecté' if BACKUP_INTERVAL_MIN <= RPO_MIN else 'NON respecté'}")
print(f"RTO {RTO_MIN} min: {'respecté' if MEASURED_RESTORE_MIN <= RTO_MIN else 'NON respecté'}")
