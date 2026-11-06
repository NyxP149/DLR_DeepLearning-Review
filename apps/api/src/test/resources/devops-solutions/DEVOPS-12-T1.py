RESOURCES = [
    ("Ingress", "shop"),
    ("Deployment", "worker"),
    ("Service", "shop"),
    ("Secret", "shop-secrets"),
    ("Namespace", "shop"),
    ("Deployment", "shop"),
    ("ConfigMap", "shop-config"),
]
RANK = {"Namespace": 0, "ConfigMap": 1, "Secret": 2, "Deployment": 3, "Service": 4, "Ingress": 5}
ordered = sorted(RESOURCES, key=lambda item: (RANK[item[0]], item[1]))
for number, (kind, name) in enumerate(ordered, start=1):
    print(f"{number}. {kind}/{name}")
