import tracemalloc

N = 100_000


def sum_squares_list(n: int) -> int:
    squares = [i * i for i in range(n)]
    return sum(squares)


def sum_squares_generator(n: int) -> int:
    return sum(i * i for i in range(n))


def peak(func, n: int) -> tuple[int, int]:
    tracemalloc.start()
    result = func(n)
    peak_bytes = tracemalloc.get_traced_memory()[1]
    tracemalloc.stop()
    return result, peak_bytes


result_list, peak_list = peak(sum_squares_list, N)
result_generator, peak_generator = peak(sum_squares_generator, N)
print(f"Résultats identiques: {result_list == result_generator}")
print(f"Pic liste > 500 Ko: {peak_list > 500_000}")
print(f"Pic générateur < 10 Ko: {peak_generator < 10_000}")
