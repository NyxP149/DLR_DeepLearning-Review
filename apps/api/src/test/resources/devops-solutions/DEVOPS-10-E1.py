def rolling(replicas: int, surge: int, unavailable: int) -> tuple[list[tuple[int, int]], bool]:
    old, new = replicas, 0
    steps: list[tuple[int, int]] = []
    while new < replicas:
        create = min(replicas - new, replicas + surge - (old + new))
        new += create
        remove = min(old, old + new - (replicas - unavailable))
        old -= remove
        if create == 0 and remove == 0:
            return steps, True
        steps.append((old, new))
    return steps, False


for surge, unavailable in [(1, 1), (0, 0)]:
    print(f"maxSurge={surge} maxUnavailable={unavailable}")
    steps, blocked = rolling(4, surge, unavailable)
    for number, (old, new) in enumerate(steps, start=1):
        print(f"  étape {number}: anciens={old} nouveaux={new}")
    if blocked:
        print("  bloqué : aucune marge pour remplacer un pod")
    else:
        print(f"  terminé en {len(steps)} étapes")
