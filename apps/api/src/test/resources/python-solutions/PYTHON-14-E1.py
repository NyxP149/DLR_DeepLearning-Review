from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-LOGGING", True)
assert evidence.validated
print("PYTHON-14: preuve validée")
