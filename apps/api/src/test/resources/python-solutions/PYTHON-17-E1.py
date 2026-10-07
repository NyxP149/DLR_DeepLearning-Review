from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-ALGORITHMS", True)
assert evidence.validated
print("PYTHON-17: preuve validée")
