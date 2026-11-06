MANIFESTS = [
    {"kind": "ConfigMap", "metadata": {"name": "shop-config"}},
    {"kind": "Secret", "metadata": {"name": "shop-secrets"}},
    {
        "kind": "Deployment",
        "metadata": {"name": "shop"},
        "spec": {
            "replicas": 1,
            "selector": {"matchLabels": {"app": "shop"}},
            "template": {
                "metadata": {"labels": {"app": "shop"}},
                "spec": {
                    "containers": [
                        {
                            "name": "shop",
                            "image": "shop:1.0.0",
                            "ports": [{"containerPort": 8080}],
                            "envFrom": [{"configMapRef": {"name": "shop-config"}}],
                            "env": [
                                {"name": "DB_PASSWORD", "valueFrom": {"secretKeyRef": {"name": "shop-secret", "key": "db"}}}
                            ],
                        }
                    ]
                },
            },
        },
    },
    {
        "kind": "Service",
        "metadata": {"name": "shop"},
        "spec": {"selector": {"app": "shop"}, "ports": [{"port": 80, "targetPort": 80}]},
    },
]

deployment = next(m for m in MANIFESTS if m["kind"] == "Deployment")
service = next(m for m in MANIFESTS if m["kind"] == "Service")
container = deployment["spec"]["template"]["spec"]["containers"][0]
labels = deployment["spec"]["template"]["metadata"]["labels"]
names = {(m["kind"], m["metadata"]["name"]) for m in MANIFESTS}
problems = 0


def report(ok: bool, success: str, failure: str) -> None:
    global problems
    if ok:
        print(f"OK    {success}")
    else:
        print(f"ÉCHEC {failure}")
        problems += 1


replicas = deployment["spec"]["replicas"]
report(replicas >= 2, f"replicas={replicas}", f"replicas={replicas} -> prévois au moins 2 réplicas")

selector = service["spec"]["selector"]
report(all(labels.get(key) == value for key, value in selector.items()),
       "le Service sélectionne les pods du Deployment",
       "le sélecteur du Service ne trouve aucun pod -> aligne les étiquettes")

target = service["spec"]["ports"][0]["targetPort"]
ports = [p["containerPort"] for p in container["ports"]]
report(target in ports, f"targetPort {target} écouté par le conteneur",
       f"targetPort {target} absent des ports {ports} -> aligne targetPort sur containerPort")

config = container["envFrom"][0]["configMapRef"]["name"]
report(("ConfigMap", config) in names, f"ConfigMap {config} trouvé",
       f"ConfigMap {config} introuvable -> crée-le ou corrige le nom")

secret = container["env"][0]["valueFrom"]["secretKeyRef"]["name"]
report(("Secret", secret) in names, f"Secret {secret} trouvé",
       f"Secret {secret} introuvable -> crée-le ou corrige le nom")

print(f"Verdict: {problems} problème(s)")
