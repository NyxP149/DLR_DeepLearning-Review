from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class Evidence:
    concept: str
    validated: bool

evidence = Evidence("PYTHON-OBSERVABILITY", True)
assert evidence.validated
print("PYTHON-22: preuve validée")
