from itertools import islice

produced = 0


def numbers():
    global produced
    value = 0
    while True:
        value += 1
        produced += 1
        yield value


def batches(iterable, size):
    iterator = iter(iterable)
    while batch := list(islice(iterator, size)):
        yield batch


source = batches(numbers(), 3)
print(f"Avant le premier lot: {produced} valeur(s) produite(s)")
for index in (1, 2):
    batch = next(source, None)
    print(f"lot {index}: {batch} ({produced} produites)")
