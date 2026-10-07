from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-PACKAGING", True)
assert evidence.validated
print("PYTHON-15: preuve validée")
