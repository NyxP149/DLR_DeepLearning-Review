from typing import Protocol


class UserRepository(Protocol):
    def exists(self, email: str) -> bool: ...
    def save(self, name: str, email: str) -> None: ...
    def names(self) -> list[str]: ...


class Mailer(Protocol):
    def send(self, to: str, subject: str) -> None: ...


class InMemoryUsers:
    def __init__(self) -> None:
        self.rows: dict[str, str] = {}

    def exists(self, email: str) -> bool:
        return email in self.rows

    def save(self, name: str, email: str) -> None:
        self.rows[email] = name

    def names(self) -> list[str]:
        return list(self.rows.values())


class FakeMailer:
    def __init__(self) -> None:
        self.sent: list[tuple[str, str]] = []

    def send(self, to: str, subject: str) -> None:
        self.sent.append((to, subject))


class RegisterUser:
    def __init__(self, users: UserRepository, mailer: Mailer) -> None:
        self.users = users
        self.mailer = mailer

    def execute(self, name: str, email: str) -> str:
        if self.users.exists(email):
            return "refusée (déjà inscrite)"
        self.users.save(name, email)
        self.mailer.send(email, "Bienvenue")
        return f"ok (email envoyé à {email})"


users = InMemoryUsers()
mailer = FakeMailer()
register = RegisterUser(users, mailer)
print(f"Inscription Ada: {register.execute('Ada', 'ada@dlr.dev')}")
print(f"Inscription Ada: {register.execute('Ada', 'ada@dlr.dev')}")
print(f"Utilisateurs: {users.names()}")
print(f"Emails: {len(mailer.sent)}")
