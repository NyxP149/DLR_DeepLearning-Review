from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-ARCHITECTURE", True)
assert evidence.validated
print("PYTHON-20: preuve validée")
