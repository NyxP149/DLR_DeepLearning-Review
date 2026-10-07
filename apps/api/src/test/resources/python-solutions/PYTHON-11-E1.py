from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-TESTS", True)
assert evidence.validated
print("PYTHON-11: preuve validée")
