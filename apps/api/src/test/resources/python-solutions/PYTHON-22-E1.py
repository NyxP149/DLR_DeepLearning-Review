import json

metrics = {"requests": 0, "errors": 0}


def log(level: str, event: str, **fields) -> None:
    print(json.dumps({"level": level, "event": event, **fields}, sort_keys=True, ensure_ascii=False))


def handle(correlation_id: str, order_id: int, balance: int, price: int) -> None:
    metrics["requests"] += 1
    if balance >= price:
        log("INFO", "commande créée", correlation_id=correlation_id, order_id=order_id)
    else:
        metrics["errors"] += 1
        log("ERROR", "paiement refusé", correlation_id=correlation_id, reason="fonds insuffisants")


def report() -> str:
    rate = round(100 * metrics["errors"] / metrics["requests"])
    return f"metrics: requests={metrics['requests']} errors={metrics['errors']} error_rate={rate}%"


handle("req-1", 42, balance=100, price=60)
handle("req-2", 43, balance=10, price=60)
print(report())
