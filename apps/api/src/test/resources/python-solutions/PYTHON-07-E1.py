from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-MODULES", True)
assert evidence.validated
print("PYTHON-07: preuve validée")
