chains = [
    {"lint": True, "test": True, "build": True, "publish": True},
    {"lint": True, "test": False, "build": True, "publish": True},
]


def run_step(name, ok, context):
    if not ok:
        return False, "2 tests en echec" if name == "test" else "echec"
    if name == "build":
        context["artifact"] = "dlr-lib-2.0.0.whl"
        return True, f"artefact {context['artifact']}"
    if name == "publish":
        return True, f"publie {context['artifact']}"
    return True, "ok"


for number, chain in enumerate(chains, start=1):
    print(f"Chaine {number}")
    context = {}
    stopped = False
    executed = 0
    for name, ok in chain.items():
        if stopped:
            print(f"[{name}] IGNORE")
            continue
        success, message = run_step(name, ok, context)
        executed += 1
        print(f"[{name}] {'OK' if success else 'ECHEC'}: {message}")
        if not success:
            stopped = True
    print(f"Livraison: {'ECHEC' if stopped else 'OK'} ({executed}/{len(chain)} etapes executees)")
