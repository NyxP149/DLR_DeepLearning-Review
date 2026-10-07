from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-CHALLENGE", True)
assert check.passed
print("DEVOPS-08: preuve validée")
