MODULES = {
    "catalogue": {"owns": ["product"], "exports": ["find_product", "list_products"], "uses": []},
    "commandes": {"owns": ["order"], "exports": ["place_order"], "uses": [("catalogue", "find_product"), ("identité", "current_user"), ("catalogue", "product_table")]},
    "identité": {"owns": ["user"], "exports": ["current_user"], "uses": []},
}


def find_cycle() -> list[str] | None:
    graph = {name: sorted({target for target, _ in info["uses"]}) for name, info in MODULES.items()}

    def visit(node: str, path: list[str]) -> list[str] | None:
        if node in path:
            return path[path.index(node):] + [node]
        for target in graph[node]:
            found = visit(target, path + [node])
            if found:
                return found
        return None

    for start in graph:
        found = visit(start, [])
        if found:
            return found
    return None


for name, info in MODULES.items():
    for entity in info["owns"]:
        print(f"Propriétaire {entity}: {name}")
for name, info in MODULES.items():
    for target, symbol in info["uses"]:
        ok = symbol in MODULES[target]["exports"]
        print(f"{name} -> {target}.{symbol}: {'OK' if ok else 'INTERDIT (non exporté)'}")
cycle = find_cycle()
print("Cycle: " + (" -> ".join(cycle) if cycle else "aucun"))
for name, info in MODULES.items():
    outgoing = {target for target, _ in info["uses"]}
    incoming = {other for other, data in MODULES.items() if any(target == name for target, _ in data["uses"])}
    print(f"{name}: sortantes={len(outgoing)}, entrantes={len(incoming)}")
