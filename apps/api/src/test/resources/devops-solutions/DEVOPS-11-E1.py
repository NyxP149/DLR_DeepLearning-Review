CONTAINERS = [
    {
        "name": "api",
        "image": "api:2.1.0",
        "env": [
            {"name": "DB_PASSWORD", "value": "admin123"},
            {"name": "LOG_LEVEL", "value": "info"},
            {"name": "API_TOKEN", "valueFrom": {"secretKeyRef": {"name": "api", "key": "token"}}},
        ],
        "readinessProbe": {"httpGet": {"path": "/ready", "port": 8080}},
        "resources": {"requests": {"cpu": "100m", "memory": "128Mi"}},
    },
    {
        "name": "log-agent",
        "image": "fluent-bit:3.1",
        "readinessProbe": {"httpGet": {"path": "/", "port": 2020}},
        "livenessProbe": {"httpGet": {"path": "/", "port": 2020}},
        "resources": {"limits": {"memory": "64Mi"}},
    },
]
SENSITIVE = ("PASSWORD", "TOKEN", "SECRET", "KEY")


def audit(container: dict) -> list[str]:
    name = container["name"]
    findings: list[str] = []
    for variable in container.get("env", []):
        if any(word in variable["name"] for word in SENSITIVE) and "value" in variable:
            findings.append(f"{name}: {variable['name']} en clair -> utilise secretKeyRef")
    if "readinessProbe" not in container:
        findings.append(f"{name}: pas de readinessProbe -> du trafic peut arriver trop tôt")
    if "livenessProbe" not in container:
        findings.append(f"{name}: pas de livenessProbe -> un blocage ne sera pas détecté")
    if "memory" not in container.get("resources", {}).get("limits", {}):
        findings.append(f"{name}: pas de limite mémoire -> risque d'OOM ou d'éviction")
    return findings


findings = [finding for container in CONTAINERS for finding in audit(container)]
for finding in findings:
    print(finding)
print(f"Problèmes: {len(findings)}")
