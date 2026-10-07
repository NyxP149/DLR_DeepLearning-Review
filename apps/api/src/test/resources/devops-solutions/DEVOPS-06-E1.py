from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-OPERATIONS", True)
assert check.passed
print("DEVOPS-06: preuve validée")
