BAD = """FROM python:latest
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
CMD ["python", "main.py"]"""

GOOD = """FROM python:3.13-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
USER app
CMD ["python", "main.py"]"""


def lint(dockerfile: str) -> list[str]:
    lines = [line.strip() for line in dockerfile.splitlines() if line.strip()]
    problems: list[str] = []

    base = next(line for line in lines if line.startswith("FROM")).split()[1]
    if ":" not in base or base.endswith(":latest"):
        problems.append(f"image de base non épinglée ({base})")

    if not any(line.startswith("USER ") and line.split()[1] != "root" for line in lines):
        problems.append("aucun USER non-root")

    install = next((i for i, line in enumerate(lines) if line.startswith("RUN") and "pip install" in line), None)
    copy_all = next((i for i, line in enumerate(lines) if line.startswith("COPY . ")), None)
    if install is not None and copy_all is not None and copy_all < install:
        problems.append("COPY . avant l'installation des dépendances (cache invalidé)")
    return problems


for label, dockerfile in (("mauvais", BAD), ("bon", GOOD)):
    problems = lint(dockerfile)
    print(f"Dockerfile {label}: {len(problems)} problème(s)")
    for problem in problems:
        print(f"- {problem}")
