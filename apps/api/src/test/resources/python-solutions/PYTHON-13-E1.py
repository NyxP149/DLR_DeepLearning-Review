from dataclasses import dataclass


@dataclass(frozen=True)
class User:
    id: int
    name: str


class HttpError(Exception):
    def __init__(self, status: int) -> None:
        super().__init__(f"erreur HTTP {status}")
        self.status = status


def parse_user(payload: dict) -> User:
    for field, kind in (("id", int), ("name", str)):
        if field not in payload:
            raise ValueError(f"contrat violé: champ '{field}' manquant")
        value = payload[field]
        if not isinstance(value, kind) or isinstance(value, bool):
            raise ValueError(f"contrat violé: champ '{field}' doit être {kind.__name__}")
    return User(payload["id"], payload["name"])


def get_user(transport, user_id: int) -> User:
    status, body = transport(f"/users/{user_id}")
    if status != 200:
        raise HttpError(status)
    return parse_user(body)


responses = {
    "/users/1": (200, {"id": 1, "name": "Ada"}),
    "/users/2": (200, {"id": "2", "name": "Linus"}),
    "/users/3": (200, {"name": "Grace"}),
    "/users/4": (404, {}),
}

for user_id in (1, 2, 3, 4):
    try:
        print(f"{user_id} -> {get_user(responses.__getitem__, user_id)}")
    except (ValueError, HttpError) as error:
        print(f"{user_id} -> {error}")
