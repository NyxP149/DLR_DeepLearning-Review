responses = [
    (200, {"id": 1, "total": 19.9, "items": ["a", "b"]}),
    (200, {"id": 2, "total": "gratuit", "items": []}),
    (200, {"id": 3, "total": 5}),
    (503, {}),
]


class HttpError(Exception):
    pass


class ContractError(Exception):
    pass


def parse_order(body):
    contract = {"id": (int,), "total": (int, float), "items": (list,)}
    labels = {"id": "un entier", "total": "un nombre", "items": "une liste"}
    for field, types in contract.items():
        if field not in body:
            raise ContractError(f"champ '{field}' manquant")
        if isinstance(body[field], bool) or not isinstance(body[field], types):
            raise ContractError(f"champ '{field}' doit etre {labels[field]}")
    return body["id"], body["total"], len(body["items"])


def get_order(status, body):
    if status != 200:
        raise HttpError(f"erreur HTTP {status}")
    return parse_order(body)


for index, (status, body) in enumerate(responses, start=1):
    try:
        order_id, total, count = get_order(status, body)
        print(f"{index} -> Order(id={order_id}, total={total}, items={count})")
    except ContractError as error:
        print(f"{index} -> contrat viole: {error}")
    except HttpError as error:
        print(f"{index} -> {error}")
