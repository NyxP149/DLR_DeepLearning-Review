modules = {
    "catalogue": {"owns": ["product"], "exports": ["find_product"]},
    "commandes": {"owns": ["order"], "exports": ["place_order"]},
    "stock": {"owns": ["inventory"], "exports": ["reserve"]},
}
usages = [
    ("commandes", "catalogue", "find_product"),
    ("commandes", "stock", "reserve"),
    ("stock", "catalogue", "product_table"),
    ("catalogue", "commandes", "place_order"),
]

for name, info in modules.items():
    for entity in info["owns"]:
        print(f"Proprietaire {entity}: {name}")
graph = {name: [] for name in modules}
incoming = {name: 0 for name in modules}
for caller, callee, symbol in usages:
    allowed = symbol in modules[callee]["exports"]
    print(f"{caller} -> {callee}.{symbol}: {'OK' if allowed else 'INTERDIT (non exporte)'}")
    graph[caller].append(callee)
    incoming[callee] += 1


def find_cycle():
    def visit(node, path):
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


cycle = find_cycle()
print("Cycle: " + (" -> ".join(cycle) if cycle else "aucun"))
for name in modules:
    print(f"{name}: entrant={incoming[name]} sortant={len(graph[name])}")
