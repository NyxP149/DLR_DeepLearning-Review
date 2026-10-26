OPTIONS = {
    "monolithe_modulaire": {"delivery": 5, "reliability": 4, "cost": 5, "weeks": 10, "euros": 1800},
    "microservices": {"delivery": 2, "reliability": 3, "cost": 2, "weeks": 20, "euros": 4500},
    "serverless": {"delivery": 4, "reliability": 3, "cost": 3, "weeks": 11, "euros": 2900},
}
MAX_WEEKS, MAX_EUROS = 12, 3000
WEIGHTS = {"delivery": 3, "reliability": 2, "cost": 2}
REEVALUATION = ["charge au pic > 2000 req/s", "équipe > 12 personnes", "un module exige 99,99 % de disponibilité"]


def violations(option: dict) -> list[str]:
    found = []
    if option["weeks"] > MAX_WEEKS:
        found.append("délai")
    if option["euros"] > MAX_EUROS:
        found.append("budget")
    return found


def score(option: dict) -> int:
    return sum(option[criterion] * weight for criterion, weight in WEIGHTS.items())


eligible = {}
rejected = []
for name, option in OPTIONS.items():
    problems = violations(option)
    if problems:
        rejected.append(name)
        print(f"{name}: écartée ({', '.join(problems)})")
    else:
        eligible[name] = option
        print(f"{name}: score {score(option)}")
winner = max(sorted(eligible), key=lambda name: score(eligible[name]))
print(f"Décision: {winner} (score {score(eligible[winner])})")
print("ADR:")
print(f"  Contexte: équipe de 4, {MAX_WEEKS} semaines, {MAX_EUROS} euros par mois")
print(f"  Options écartées: {', '.join(rejected)}")
print("  Conséquence: déploiement unique, frontières internes à contrôler en CI")
print("  Réévaluer si: " + " ; ".join(REEVALUATION))
