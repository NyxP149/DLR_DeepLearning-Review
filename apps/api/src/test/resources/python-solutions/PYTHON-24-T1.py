times = [0, 1, 2, 5, 6, 6.5]


class SlidingWindow:
    def __init__(self, limit, window):
        self.limit = limit
        self.window = window
        self.accepted = []

    def allow(self, now):
        self.accepted = [t for t in self.accepted if t > now - self.window]
        if len(self.accepted) >= self.limit:
            return False
        self.accepted.append(now)
        return True


limiter = SlidingWindow(2, 5)
allowed = 0
for now in times:
    verdict = limiter.allow(now)
    allowed += verdict
    print(f"t={now}: {'autorisee' if verdict else 'refusee'}")
print(f"Autorisees: {allowed}/{len(times)}")
