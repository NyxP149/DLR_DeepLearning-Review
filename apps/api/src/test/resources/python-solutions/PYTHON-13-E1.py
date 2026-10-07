from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-HTTP", True)
assert evidence.validated
print("PYTHON-13: preuve validée")
