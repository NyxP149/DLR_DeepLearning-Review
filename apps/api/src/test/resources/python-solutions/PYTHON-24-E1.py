class TokenBucket:
    def __init__(self, capacity: int, refill_per_second: float) -> None:
        self.capacity = capacity
        self.refill_per_second = refill_per_second
        self.tokens = float(capacity)
        self.last = 0.0

    def allow(self, now: float) -> bool:
        self.tokens = min(self.capacity, self.tokens + (now - self.last) * self.refill_per_second)
        self.last = now
        if self.tokens >= 1:
            self.tokens -= 1
            return True
        return False


bucket = TokenBucket(capacity=3, refill_per_second=1.0)
allowed = 0
times = [0.0, 0.0, 0.0, 0.0, 1.0, 1.5, 2.5]
for now in times:
    decision = bucket.allow(now)
    allowed += decision
    print(f"t={now}: {'autorisée' if decision else 'refusée'}")
print(f"Autorisées: {allowed}/{len(times)}")
