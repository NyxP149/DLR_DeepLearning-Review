import tracemalloc


def peak_of(function):
    tracemalloc.start()
    result = function()
    _, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    return result, peak


def with_list():
    return sum([len(str(n)) for n in range(50000)])


def with_generator():
    return sum(len(str(n)) for n in range(50000))


list_result, list_peak = peak_of(with_list)
generator_result, generator_peak = peak_of(with_generator)
print("Resultats identiques:", list_result == generator_result)
print("Pic liste > 200 Ko:", list_peak > 200 * 1024)
print("Pic generateur < 10 Ko:", generator_peak < 10 * 1024)
