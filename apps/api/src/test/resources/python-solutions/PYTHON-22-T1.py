import json

counters = {"requests": 0, "errors": 0}


def log(level, event, **fields):
    print(json.dumps({"level": level, "event": event, **fields}, sort_keys=True, ensure_ascii=False))


def handle(correlation_id, ok):
    counters["requests"] += 1
    if ok:
        log("INFO", "demande traitée", correlation_id=correlation_id)
    else:
        counters["errors"] += 1
        log("ERROR", "demande échouée", correlation_id=correlation_id)


def report():
    rate = 100 * counters["errors"] / counters["requests"]
    print(f"requetes={counters['requests']} erreurs={counters['errors']} taux_erreur={rate:.1f}%")


handle("req-1", True)
handle("req-2", False)
handle("req-3", True)
report()
