services = {"gateway": "healthy", "orders": "healthy", "billing": "degraded"}
release = {"version": "2.0", "traffic_percent": 10}
ready = all(state == "healthy" for state in services.values())
print("Canary:", release["traffic_percent"], "%")
print("Promotion:", "GO" if ready else "STOP")
print("Service à traiter:", next(name for name, state in services.items() if state != "healthy"))
