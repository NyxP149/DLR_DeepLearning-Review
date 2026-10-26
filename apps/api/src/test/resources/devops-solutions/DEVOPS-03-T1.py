import re

CONFIG = """region=eu-west-1
aws_key=AKIAABCDEFGHIJKLMNOP
mode=prod
password=hunter2
-----BEGIN RSA PRIVATE KEY-----
"""
risks = 0
for number, line in enumerate(CONFIG.strip().splitlines(), start=1):
    found = re.search(r"AKIA[0-9A-Z]{16}", line)
    if found:
        key = found.group(0)
        print(f"ligne {number}: cle AWS detectee ({key[:4]}{'*' * (len(key) - 8)}{key[-4:]})")
        risks += 1
    if re.search(r"-----BEGIN [A-Z ]*PRIVATE KEY-----", line):
        print(f"ligne {number}: cle privee detectee")
        risks += 1
    if re.match(r"password\s*=\s*\S+", line):
        print(f"ligne {number}: mot de passe en clair detecte")
        risks += 1
print(f"Risques: {risks}")
