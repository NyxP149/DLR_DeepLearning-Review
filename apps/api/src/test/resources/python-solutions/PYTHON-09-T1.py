import functools
from contextlib import contextmanager

settings = {"debug": False}
calls = 0


def memoize(function):
    cache = {}

    @functools.wraps(function)
    def wrapper(n):
        global calls
        if n not in cache:
            calls += 1
            cache[n] = function(n)
        return cache[n]

    return wrapper


@memoize
def fib(n):
    return n if n < 2 else fib(n - 1) + fib(n - 2)


@contextmanager
def temporary(target, key, value):
    previous = target[key]
    target[key] = value
    try:
        yield
    finally:
        target[key] = previous


print(f"fib(10) = {fib(10)} | appels reels: {calls} | nom conserve: {fib.__name__}")
print("Avant: debug=" + str(settings["debug"]))
with temporary(settings, "debug", True):
    print("Dans le contexte: debug=" + str(settings["debug"]))
print("Apres: debug=" + str(settings["debug"]))
try:
    with temporary(settings, "debug", True):
        raise RuntimeError("boom")
except RuntimeError:
    pass
print("Apres erreur: debug=" + str(settings["debug"]))
