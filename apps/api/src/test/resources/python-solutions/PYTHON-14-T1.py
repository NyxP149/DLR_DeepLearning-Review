import logging
import sys


def load_config(env):
    return {
        "timeout": float(env.get("TIMEOUT", "2.5")),
        "retries": int(env.get("RETRIES", "3")),
        "verbose": env.get("VERBOSE", "false").strip().lower() in ("1", "true", "yes"),
    }


def setup_logging(verbose):
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(level=level, format="%(levelname)s %(name)s: %(message)s", stream=sys.stdout, force=True)
    return logging.getLevelName(level)


for env in ({"TIMEOUT": "1.5", "RETRIES": "5", "VERBOSE": "true"}, {}):
    config = load_config(env)
    level = setup_logging(config["verbose"])
    print(f"Config: timeout={config['timeout']} retries={config['retries']} verbose={config['verbose']} level={level}")
    log = logging.getLogger("svc")
    log.debug("detail")
    log.info("demarrage")
