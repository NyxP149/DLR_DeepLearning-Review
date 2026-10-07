ENV = ["DB_HOST=db", "DB_PASSWORD=admin123", "API_TOKEN=${API_TOKEN}", "LOG_LEVEL=info"]
CONTAINER = {"privileged": True, "user": "root"}
SENSITIVE = ("PASSWORD", "TOKEN", "SECRET", "KEY")


def scan_env(lines: list[str]) -> list[str]:
    findings: list[str] = []
    for line in lines:
        key, _, value = line.partition("=")
        if any(word in key for word in SENSITIVE) and not value.startswith("${"):
            findings.append(f"{key}: secret en clair -> utilise un secret Docker")
    return findings


def scan_container(config: dict) -> list[str]:
    findings: list[str] = []
    if config["privileged"]:
        findings.append("privileged: true -> retire le mode privilégié")
    if config["user"] == "root":
        findings.append("user: root -> utilise un utilisateur non-root")
    return findings


findings = scan_env(ENV) + scan_container(CONTAINER)
for finding in findings:
    print(finding)
print(f"Risques: {len(findings)}")
