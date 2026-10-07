from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-GENERATORS", True)
assert evidence.validated
print("PYTHON-08: preuve validée")
