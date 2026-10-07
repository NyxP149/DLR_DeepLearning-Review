from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-CHALLENGE", True)
assert evidence.validated
print("PYTHON-24: preuve validée")
