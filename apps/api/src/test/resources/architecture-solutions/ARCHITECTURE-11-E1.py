services = {"web": ["api"], "api": ["db"], "db": []}
healthy = {"db"}
for name in ("db", "api", "web"):
    if all(dep in healthy for dep in services[name]):
        healthy.add(name)
        print(name, "HEALTHY")
