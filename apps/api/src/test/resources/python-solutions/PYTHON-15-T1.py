import tomllib

TEXT = """
[project]
name = "dlr-etl"
version = "0.4.1"
requires-python = ">=3.12"
dependencies = ["requests", "pydantic>=2,<3", "rich"]

[project.scripts]
etl = "dlr_etl.main:run"
"""
project = tomllib.loads(TEXT)["project"]
bounds = (">=", "==", "~=", "<", "!=")
unbounded = sorted(dep for dep in project["dependencies"] if not any(op in dep for op in bounds))
print(f"Projet: {project['name']} {project['version']}")
print(f"Python requis: {project['requires-python']}")
print("Dependances sans borne:", ", ".join(unbounded))
for name, target in project["scripts"].items():
    print(f"Script: {name} -> {target}")
