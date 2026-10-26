DEFAULTS = {"artifact": "billing:2.1.0", "log": "INFO", "pool": "5", "db": "jdbc:h2:mem:x", "DB_PASSWORD": "${VAULT_DB}"}
FILES = {
    "staging": {"log": "DEBUG", "db": "jdbc:postgresql://stg-db/billing"},
    "production": {"pool": "20", "db": "jdbc:postgresql://prod-db/billing", "DB_PASSWORD": "hunter2"},
}
ENVIRONMENT = {"production": {"log": "WARN"}}
KNOWN = {"log", "pool", "db"}


def resolve(name):
    config = dict(DEFAULTS)
    config.update(FILES.get(name, {}))
    config.update({key: value for key, value in ENVIRONMENT.get(name, {}).items() if key in KNOWN})
    return config


for name in ("staging", "production"):
    config = resolve(name)
    print(f"{name}: {config['artifact']} log={config['log']} pool={config['pool']} db={config['db']}")
    plain = sorted(
        key for key, value in config.items()
        if ("PASSWORD" in key or "TOKEN" in key) and not (value.startswith("${") and value.endswith("}"))
    )
    print("  secrets en clair: " + (", ".join(plain) if plain else "aucun"))
