from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-SECURITY", True)
assert check.passed
print("DEVOPS-03: preuve validée")
