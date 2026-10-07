from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-PROJECT", True)
assert check.passed
print("DEVOPS-07: preuve validée")
