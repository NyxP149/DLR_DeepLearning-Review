import logging
import sys


def load_config(env: dict) -> dict:
    debug = env.get("DEBUG", "false").lower() in ("1", "true", "yes")
    return {
        "port": int(env.get("PORT", "8000")),
        "debug": debug,
        "level": "DEBUG" if debug else "INFO",
    }


def setup_logging(level: str) -> logging.Logger:
    logging.basicConfig(
        level=level,
        format="%(levelname)s %(name)s: %(message)s",
        stream=sys.stdout,
        force=True,
    )
    return logging.getLogger("app")


for env in ({"PORT": "8080", "DEBUG": "true"}, {}):
    config = load_config(env)
    print(f"Config: port={config['port']} debug={config['debug']} level={config['level']}")
    logger = setup_logging(config["level"])
    logger.debug("diagnostic")
    logger.info("démarrage")
    logger.warning("attention")
