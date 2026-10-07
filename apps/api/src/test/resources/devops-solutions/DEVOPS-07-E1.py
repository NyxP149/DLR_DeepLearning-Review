class StageError(Exception):
    pass


def build(ctx: dict) -> str:
    ctx["artifact"] = "dlr-api:1.4.0"
    return f"artefact {ctx['artifact']}"


def test(ctx: dict) -> str:
    return "42 tests"


def scan_failing(ctx: dict) -> str:
    raise StageError("1 vulnérabilité critique")


def scan_clean(ctx: dict) -> str:
    return "aucune vulnérabilité"


def deploy(ctx: dict) -> str:
    return f"{ctx['artifact']} déployé"


def run(stages: list) -> None:
    ctx: dict = {}
    executed = 0
    failed = False
    for name, stage in stages:
        if failed:
            print(f"[{name}] IGNORÉ")
            continue
        executed += 1
        try:
            print(f"[{name}] OK: {stage(ctx)}")
        except StageError as error:
            print(f"[{name}] ÉCHEC: {error}")
            failed = True
    verdict = "ÉCHEC" if failed else "SUCCÈS"
    print(f"Livraison: {verdict} ({executed}/{len(stages)} étapes exécutées)")


run([("build", build), ("test", test), ("scan", scan_failing), ("deploy", deploy)])
run([("build", build), ("test", test), ("scan", scan_clean), ("deploy", deploy)])
