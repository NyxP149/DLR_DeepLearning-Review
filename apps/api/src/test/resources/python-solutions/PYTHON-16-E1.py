from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-PERSISTENCE", True)
assert evidence.validated
print("PYTHON-16: preuve validée")
