JOBS = {
    "install": (3, []),
    "lint": (2, ["install"]),
    "test": (6, ["install"]),
    "build": (4, ["install"]),
    "package": (3, ["build", "test"]),
}


def critical_path(jobs: dict) -> tuple[int, list[str]]:
    results: dict[str, tuple[int, list[str]]] = {}

    def finish(name: str) -> tuple[int, list[str]]:
        if name in results:
            return results[name]
        duration, needs = jobs[name]
        if needs:
            previous = max((finish(need) for need in needs), key=lambda result: result[0])
            result = (previous[0] + duration, previous[1] + [name])
        else:
            result = (duration, [name])
        results[name] = result
        return result

    return max((finish(name) for name in jobs), key=lambda result: result[0])


sequential = sum(duration for duration, _ in JOBS.values())
parallel, path = critical_path(JOBS)
print(f"Séquentiel: {sequential} min")
print(f"Parallèle: {parallel} min")
print(f"Chemin critique: {' -> '.join(path)}")

cached = dict(JOBS)
cached["install"] = (1, [])
print(f"Avec cache: {critical_path(cached)[0]} min")
