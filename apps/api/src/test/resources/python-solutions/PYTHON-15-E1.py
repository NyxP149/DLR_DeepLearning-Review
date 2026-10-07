import tomllib

PYPROJECT = """
[project]
name = "dlr-tools"
version = "1.2.0"
requires-python = ">=3.11"
dependencies = ["requests>=2.31", "flask", "pydantic>=2"]

[project.scripts]
dlr = "dlr_tools.cli:main"
"""

project = tomllib.loads(PYPROJECT)["project"]
print(f"Projet: {project['name']} {project['version']}")
print(f"Python requis: {project['requires-python']}")

operators = (">=", "==", "~=", "<", "!=")
unbounded = [dependency for dependency in project["dependencies"] if not any(op in dependency for op in operators)]
print(f"Dépendances sans borne: {', '.join(unbounded) if unbounded else 'aucune'}")

for command, target in project["scripts"].items():
    print(f"Script: {command} -> {target}")
