from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-TYPING", True)
assert evidence.validated
print("PYTHON-10: preuve validée")
