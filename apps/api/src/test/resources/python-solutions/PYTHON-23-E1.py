from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-PROJECT", True)
assert evidence.validated
print("PYTHON-23: preuve validée")
