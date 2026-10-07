from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-ASYNC", True)
assert evidence.validated
print("PYTHON-12: preuve validée")
