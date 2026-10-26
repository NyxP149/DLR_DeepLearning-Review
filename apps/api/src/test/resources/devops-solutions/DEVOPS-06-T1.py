deployments = [
    ("1.4.0", [True, True, True]),
    ("1.5.0", [True, False, True, True, False]),
]
active = "1.3.9"
for version, checks in deployments:
    print(f"Deploiement {version}")
    healthy = 0
    switched = False
    for number, ok in enumerate(checks[:5], start=1):
        healthy = healthy + 1 if ok else 0
        print(f"Controle {number}: {'sain' if ok else 'echec'} ({healthy}/3)")
        if healthy == 3:
            switched = True
            break
    if switched:
        active = version
        print(f"Bascule vers {version}")
    else:
        print(f"Abandon: {version} reste en vert, {active} reste actif")
    print(f"Version active: {active}")
