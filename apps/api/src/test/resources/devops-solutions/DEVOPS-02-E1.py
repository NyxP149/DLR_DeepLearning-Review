services_ok = {
    "web": {"healthcheck": False, "depends_on": {"api": "service_healthy"}},
    "api": {"healthcheck": True, "depends_on": {"db": "service_healthy"}},
    "db": {"healthcheck": True, "depends_on": {}},
}
services_cycle = {
    "a": {"healthcheck": True, "depends_on": {"b": "service_started"}},
    "b": {"healthcheck": True, "depends_on": {"a": "service_started"}},
}
services_fragile = {
    "worker": {"healthcheck": False, "depends_on": {"cache": "service_healthy"}},
    "cache": {"healthcheck": False, "depends_on": {}},
}


def start_order(services: dict) -> list[str]:
    order: list[str] = []
    done: set[str] = set()

    def visit(name: str, stack: list[str]) -> None:
        if name in done:
            return
        if name in stack:
            cycle = stack[stack.index(name):] + [name]
            raise ValueError("cycle: " + " -> ".join(cycle))
        for dependency in sorted(services[name]["depends_on"]):
            visit(dependency, stack + [name])
        done.add(name)
        order.append(name)

    for name in sorted(services):
        visit(name, [])
    return order


def missing_health(services: dict) -> list[str]:
    problems = []
    for name in sorted(services):
        for dependency, condition in services[name]["depends_on"].items():
            if condition == "service_healthy" and not services[dependency]["healthcheck"]:
                problems.append(f"{name} attend {dependency} sans healthcheck")
    return problems


for services in (services_ok, services_cycle, services_fragile):
    try:
        print("Ordre:", ", ".join(start_order(services)))
    except ValueError as error:
        print("Erreur:", error)
    problems = missing_health(services)
    print("Santé:", "; ".join(problems) if problems else "OK")
