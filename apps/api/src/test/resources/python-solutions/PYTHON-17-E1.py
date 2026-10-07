NUMBERS = [8, 3, 11, 7, 2, 15, 4]
TARGET = 9


def naive(numbers: list[int], target: int) -> tuple[tuple[int, int] | None, int]:
    comparisons = 0
    for i in range(len(numbers)):
        for j in range(i + 1, len(numbers)):
            comparisons += 1
            if numbers[i] + numbers[j] == target:
                return (i, j), comparisons
    return None, comparisons


def hashed(numbers: list[int], target: int) -> tuple[tuple[int, int] | None, int]:
    lookups = 0
    seen: dict[int, int] = {}
    for index, value in enumerate(numbers):
        lookups += 1
        complement = target - value
        if complement in seen:
            return (seen[complement], index), lookups
        seen[value] = index
    return None, lookups


pair_a, comparisons = naive(NUMBERS, TARGET)
pair_b, lookups = hashed(NUMBERS, TARGET)
print(f"Naïf: indices {pair_a} après {comparisons} comparaisons")
print(f"Hachage: indices {pair_b} après {lookups} consultations")
print(f"Gain: {comparisons - lookups} opérations")
