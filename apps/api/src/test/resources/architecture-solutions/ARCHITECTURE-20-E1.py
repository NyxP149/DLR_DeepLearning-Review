controls = ["TLS", "validation", "RBAC", "secrets", "audit"]
required = {"TLS", "validation", "RBAC", "secrets", "audit"}
print("Contrôles:", len(controls))
print("Prêt:", set(controls) >= required)
