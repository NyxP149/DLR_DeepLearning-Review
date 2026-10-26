import json
from typing import Protocol, runtime_checkable


@runtime_checkable
class Exporter(Protocol):
    def export(self, data: dict) -> str: ...


class JsonExporter:
    def export(self, data: dict) -> str:
        return json.dumps(data)


class CsvExporter:
    def export(self, data: dict) -> str:
        return ",".join(f"{key}={value}" for key, value in data.items())


class BrokenExporter:
    def dump(self, data: dict) -> str:
        return ""


for exporter in (JsonExporter(), CsvExporter(), BrokenExporter()):
    name = type(exporter).__name__
    if isinstance(exporter, Exporter):
        print(f"{name}: conforme -> {exporter.export({'a': 1})}")
    else:
        print(f"{name}: non conforme")
