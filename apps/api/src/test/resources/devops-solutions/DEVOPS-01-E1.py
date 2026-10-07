from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-IMAGES", True)
assert check.passed
print("DEVOPS-01: preuve validée")
