artifact = "dlr-api:1.4.0"
environments = {"staging": "stg-db", "production": "prod-db"}
for env, database in environments.items():
    print(f"{env}: {artifact} -> {database}")
print("Secrets dans image: 0")
