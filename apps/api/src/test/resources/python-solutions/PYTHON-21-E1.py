from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-FASTAPI", True)
assert evidence.validated
print("PYTHON-21: preuve validée")
