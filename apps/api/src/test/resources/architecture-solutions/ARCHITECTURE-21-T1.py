calls = [(0, False), (1, False), (2, True), (21, True), (22, True)]
BACKUP_EVERY_HOURS = 6
RESTORE_MINUTES = 45
RPO_HOURS = 4
RTO_MINUTES = 60

delays = [min(0.5 * 2 ** attempt, 4.0) for attempt in range(5)]
print("Delais de reessai: " + ", ".join(f"{delay:.1f}" for delay in delays))


class Breaker:
    def __init__(self):
        self.state = "closed"
        self.failures = 0
        self.opened_at = 0

    def call(self, now, succeeds):
        if self.state == "open":
            if now - self.opened_at < 20:
                return "REFUSE"
            self.state = "half-open"
        if succeeds:
            self.state = "closed"
            self.failures = 0
            return "OK"
        self.failures += 1
        if self.state == "half-open" or self.failures >= 2:
            self.state = "open"
            self.opened_at = now
        return "ECHEC"


breaker = Breaker()
for now, succeeds in calls:
    result = breaker.call(now, succeeds)
    print(f"t={now}: {result} (etat {breaker.state})")
print(f"RPO: {'respecte' if BACKUP_EVERY_HOURS <= RPO_HOURS else 'viole'} (sauvegarde toutes les {BACKUP_EVERY_HOURS} h, objectif {RPO_HOURS} h)")
print(f"RTO: {'respecte' if RESTORE_MINUTES <= RTO_MINUTES else 'viole'} (restauration {RESTORE_MINUTES} min, objectif {RTO_MINUTES} min)")
