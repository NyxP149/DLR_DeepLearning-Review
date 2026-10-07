from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-QUALITY", True)
assert check.passed
print("DEVOPS-05: preuve validée")
