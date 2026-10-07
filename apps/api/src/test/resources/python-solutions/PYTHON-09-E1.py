import functools
from contextlib import contextmanager


def retry(times: int):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            for attempt in range(1, times + 1):
                try:
                    result = func(*args, **kwargs)
                    print(f"tentative {attempt}: succès")
                    return result
                except ConnectionError as error:
                    print(f"tentative {attempt}: échec ({error})")
                    if attempt == times:
                        raise

        return wrapper

    return decorator


@contextmanager
def transaction():
    print("BEGIN")
    try:
        yield
    except Exception as error:
        print(f"ROLLBACK ({error})")
        raise
    else:
        print("COMMIT")


calls = {"count": 0}


@retry(3)
def fetch() -> str:
    calls["count"] += 1
    if calls["count"] == 1:
        raise ConnectionError("réseau")
    return "ok"


try:
    print(f"Résultat: {fetch()} | nom conservé: {fetch.__name__}")
except ConnectionError as error:
    print(f"Erreur non gérée: {error}")

with transaction():
    pass

try:
    with transaction():
        raise ValueError("boom")
except ValueError:
    pass
