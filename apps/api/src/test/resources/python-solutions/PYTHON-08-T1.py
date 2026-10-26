import itertools

read = 0


def stream():
    global read
    for value in itertools.count(1):
        read += 1
        yield value


def running_total(iterable):
    total = 0
    for value in iterable:
        total += value
        yield total


totals = running_total(stream())
print(f"Avant le premier total: {read} valeur(s) lue(s)")
for index in range(1, 4):
    print(f"total {index}: {next(totals)} ({read} lues)")
