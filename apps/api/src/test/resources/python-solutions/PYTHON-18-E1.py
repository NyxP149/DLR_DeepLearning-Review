from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-PERFORMANCE", True)
assert evidence.validated
print("PYTHON-18: preuve validée")
