PODS = [
    {"name": "web-7d9", "phase": "Pending", "reason": "Unschedulable"},
    {"name": "api-5c1", "phase": "Running", "ready": False, "reason": None},
    {"name": "worker-9f2", "phase": "Running", "ready": False, "reason": "CrashLoopBackOff", "lastReason": "OOMKilled"},
    {"name": "job-3a", "phase": "Pending", "reason": "ImagePullBackOff"},
    {"name": "db-1", "phase": "Running", "ready": True, "reason": None},
]
ENDPOINTS = {"shop": 0, "db": 1}


def diagnose(pod: dict) -> str | None:
    name = pod["name"]
    if pod["reason"] == "Unschedulable":
        return f"{name}: Pending (Unschedulable) -> manque de ressources sur les nœuds : réduis les requests ou ajoute un nœud"
    if pod["reason"] == "ImagePullBackOff":
        return f"{name}: ImagePullBackOff -> image ou tag introuvable, ou accès au registre refusé"
    if pod["reason"] == "CrashLoopBackOff":
        if pod.get("lastReason") == "OOMKilled":
            return f"{name}: CrashLoopBackOff, OOMKilled (code 137) -> augmente la limite mémoire ou corrige la fuite"
        return f"{name}: CrashLoopBackOff -> lis kubectl logs --previous"
    if pod["phase"] == "Running" and not pod.get("ready"):
        return f"{name}: Running mais non prêt -> la readiness probe échoue : vérifie chemin, port et démarrage"
    return None


findings = [message for pod in PODS if (message := diagnose(pod))]
for message in findings:
    print(message)
empty = [service for service, count in ENDPOINTS.items() if count == 0]
for service in empty:
    print(f"service {service}: aucun endpoint -> aucun pod prêt ou sélecteur incorrect")
print(f"Pods en échec: {len(findings)} sur {len(PODS)}")
print(f"Services sans endpoint: {len(empty)}")
