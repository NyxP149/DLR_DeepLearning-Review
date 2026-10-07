THRESHOLD = 5.0
active = "1.3.9"


def decide(rate: float) -> str:
    return "continuer" if rate <= THRESHOLD else "ROLLBACK"


def rollout(candidate: str, steps: list[tuple[int, float]]) -> None:
    global active
    print(f"Déploiement {candidate}")
    for percent, rate in steps:
        decision = decide(rate)
        operator = "<=" if decision == "continuer" else ">"
        print(f"Étape {percent}%: erreurs {rate:.1f}% {operator} {THRESHOLD:.1f}% -> {decision}")
        if decision == "ROLLBACK":
            print(f"Version active: {active}")
            return
    active = candidate
    print(f"Version active: {active}")


rollout("1.4.0", [(10, 0.8), (50, 6.0), (100, 0.9)])
rollout("1.5.0", [(10, 0.5), (100, 1.2)])
