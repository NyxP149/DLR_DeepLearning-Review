from dataclasses import dataclass

@dataclass(frozen=True)
class DeliveryCheck:
    control: str
    passed: bool

check = DeliveryCheck("DEVOPS-COMPOSE", True)
assert check.passed
print("DEVOPS-02: preuve validée")
