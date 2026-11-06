PODS = {
    "api": [{"requests": {"cpu": "500m", "memory": "256Mi"}, "limits": {"cpu": "500m", "memory": "256Mi"}}],
    "worker": [{"requests": {"cpu": "100m"}, "limits": {"memory": "512Mi"}}],
    "debug": [{}],
}


def qos(containers):
    if all(not c.get("requests") and not c.get("limits") for c in containers):
        return "BestEffort"
    if all(
        c.get("requests", {}).get(r) is not None and c.get("requests", {}).get(r) == c.get("limits", {}).get(r)
        for c in containers
        for r in ("cpu", "memory")
    ):
        return "Guaranteed"
    return "Burstable"


classes = {name: qos(containers) for name, containers in PODS.items()}
for name, kind in classes.items():
    print(f"{name}: {kind}")
rank = {"BestEffort": 0, "Burstable": 1, "Guaranteed": 2}
order = sorted(classes, key=lambda name: rank[classes[name]])
print("Ordre d'éviction: " + ", ".join(order))
