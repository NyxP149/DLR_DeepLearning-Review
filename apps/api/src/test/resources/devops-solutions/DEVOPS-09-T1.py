PODS = [
    {"name": "web-1", "labels": {"app": "web", "tier": "front"}},
    {"name": "web-2", "labels": {"app": "web", "tier": "front"}},
    {"name": "db-1", "labels": {"app": "db"}},
    {"name": "web-old", "labels": {"app": "web-v1"}},
]
SERVICES = {
    "front": {"app": "web"},
    "static": {"app": "cache"},
    "strict": {"app": "web", "tier": "front"},
}
empty = 0
for service, selector in SERVICES.items():
    chosen = [pod["name"] for pod in PODS
              if all(pod["labels"].get(key) == value for key, value in selector.items())]
    if chosen:
        print(f"{service}: {', '.join(chosen)}")
    else:
        print(f"{service}: AUCUN POD -> vérifie les étiquettes")
        empty += 1
print(f"Services sans pod: {empty}")
