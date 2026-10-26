import re

DOCKERFILE = """FROM python:3.13-slim
ADD app.py /app/app.py
RUN apt-get update
RUN apt-get install -y curl
ENV API_TOKEN=abc123
CMD ["python", "/app/app.py"]
"""
lines = [line.strip() for line in DOCKERFILE.strip().splitlines()]
problems = []
if any(re.match(r"ADD\s+(?!https?://)\S+\s", line) and not line.endswith(".tar.gz") for line in lines):
    problems.append("ADD utilise pour une copie locale (prefere COPY)")
update_alone = any(line.startswith("RUN apt-get update") and "install" not in line for line in lines)
if update_alone:
    problems.append("apt-get update separe de apt-get install (cache perime)")
if any(re.match(r"(ENV|ARG)\s+\w*(PASSWORD|TOKEN)\w*[= ]", line) for line in lines):
    problems.append("secret passe par ENV ou ARG")
print(f"Dockerfile mauvais: {len(problems)} probleme(s)")
for problem in problems:
    print(f"- {problem}")
