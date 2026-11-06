POD = {
    "apiVersion": "v1",
    "kind": "Pod",
    "metadata": {"name": "web", "labels": {"app": "web"}},
    "spec": {
        "containers": [
            {
                "name": "web",
                "image": "nginx:latest",
                "resources": {"limits": {"memory": "128Mi"}},
                "securityContext": {"runAsNonRoot": True},
            },
            {"name": "sidecar", "image": "busybox"},
        ]
    },
}


def audit(pod: dict) -> list[str]:
    findings: list[str] = []
    for container in pod["spec"]["containers"]:
        name = container["name"]
        image = container["image"]
        if ":" not in image or image.endswith(":latest"):
            findings.append(f"{name}: image non épinglée ({image}) -> utilise un tag de version")
        if "limits" not in container.get("resources", {}):
            findings.append(f"{name}: pas de limites de ressources -> définis resources.limits")
        if not container.get("securityContext", {}).get("runAsNonRoot"):
            findings.append(f"{name}: peut tourner en root -> ajoute runAsNonRoot: true")
    return findings


findings = audit(POD)
for finding in findings:
    print(finding)
print(f"Problèmes: {len(findings)}")
