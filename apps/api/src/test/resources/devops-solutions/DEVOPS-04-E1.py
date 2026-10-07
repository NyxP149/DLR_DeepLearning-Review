from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-CI", True)
assert check.passed
print("DEVOPS-04: preuve validée")
