from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-SECURITY", True)
assert evidence.validated
print("PYTHON-19: preuve validée")
