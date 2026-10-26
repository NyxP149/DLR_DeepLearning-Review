ARTIFACT = "dlr-api:1.4.0"
DEFAULTS = {"log_level": "INFO", "pool_size": 5, "db_url": "jdbc:postgresql://localhost/dlr", "db_password": "${DB_PASSWORD}"}
ENV_FILES = {
    "staging": {"db_url": "jdbc:postgresql://stg-db/dlr", "log_level": "DEBUG"},
    "production": {"db_url": "jdbc:postgresql://prod-db/dlr", "pool_size": 20, "db_password": "hunter2"},
}
ENV_VARS = {"production": {"log_level": "WARN"}, "staging": {}}
SENSITIVE = ("password", "token", "secret", "key")


def resolve(environment: str) -> dict:
    config = dict(DEFAULTS)
    config.update(ENV_FILES[environment])
    config.update(ENV_VARS[environment])
    return config


def leaked(config: dict) -> list[str]:
    return [key for key, value in config.items()
            if any(word in key.lower() for word in SENSITIVE) and not str(value).startswith("${")]


for environment in ("staging", "production"):
    config = resolve(environment)
    print(f"{environment}: {ARTIFACT} log={config['log_level']} pool={config['pool_size']} db={config['db_url']}")
    found = leaked(config)
    print("  secrets en clair: " + (", ".join(found) or "aucun"))
print("Même artefact partout: True")
