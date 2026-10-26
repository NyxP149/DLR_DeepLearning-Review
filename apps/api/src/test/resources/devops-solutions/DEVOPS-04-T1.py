jobs = {
    "checkout": (1, []),
    "install": (3, ["checkout"]),
    "lint": (2, ["install"]),
    "tests_a": (6, ["install"]),
    "tests_b": (4, ["install"]),
    "package": (2, ["lint", "tests_a", "tests_b"]),
}


def critical(durations):
    best = {}

    def finish(name):
        if name not in best:
            duration, deps = durations[name]
            previous = max((finish(dep) for dep in deps), key=lambda item: item[0], default=(0, []))
            best[name] = (previous[0] + duration, previous[1] + [name])
        return best[name]

    return max((finish(name) for name in durations), key=lambda item: item[0])


total, path = critical(jobs)
cached = dict(jobs)
cached["install"] = (1, jobs["install"][1])
print(f"Sequentiel: {sum(duration for duration, _ in jobs.values())} min")
print(f"Parallele: {total} min")
print(f"Chemin critique: {' -> '.join(path)}")
print(f"Avec cache: {critical(cached)[0]} min")
