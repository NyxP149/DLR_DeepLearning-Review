from typing import Protocol, runtime_checkable


@runtime_checkable
class Notifier(Protocol):
    def send(self, message: str) -> str: ...


class EmailNotifier:
    def send(self, message: str) -> str:
        return f"email: {message}"


class SmsNotifier:
    def send(self, message: str) -> str:
        return f"sms: {message}"


class Broken:
    def push(self, message: str) -> str:
        return message


def conforms(candidate: object) -> bool:
    return isinstance(candidate, Notifier)


for candidate in (EmailNotifier(), SmsNotifier(), Broken()):
    name = type(candidate).__name__
    if conforms(candidate):
        print(f"{name}: conforme -> {candidate.send('bonjour')}")
    else:
        print(f"{name}: non conforme")
