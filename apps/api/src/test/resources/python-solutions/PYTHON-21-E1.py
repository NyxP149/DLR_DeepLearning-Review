import inspect


def match(pattern: str, path: str) -> dict | None:
    expected, actual = pattern.strip("/").split("/"), path.strip("/").split("/")
    if len(expected) != len(actual):
        return None
    params: dict = {}
    for left, right in zip(expected, actual):
        if left.startswith("{"):
            params[left.strip("{}")] = int(right) if right.isdigit() else right
        elif left != right:
            return None
    return params


def get_repo() -> dict:
    return {1: "Ada"}


class App:
    def __init__(self) -> None:
        self.routes: list = []
        self.dependency_overrides: dict = {}

    def get(self, pattern: str):
        def decorator(handler):
            self.routes.append((pattern, handler))
            return handler

        return decorator

    def resolve(self, dependency):
        return self.dependency_overrides.get(dependency, dependency)()

    def dispatch(self, path: str) -> tuple[int, dict]:
        for pattern, handler in self.routes:
            params = match(pattern, path)
            if params is None:
                continue
            if "repo" in inspect.signature(handler).parameters:
                params["repo"] = self.resolve(get_repo)
            try:
                return 200, handler(**params)
            except LookupError as error:
                return 404, {"detail": str(error).strip("'")}
        return 404, {"detail": "route inconnue"}


app = App()


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.get("/users/{user_id}")
def read_user(user_id: int, repo: dict) -> dict:
    if user_id not in repo:
        raise LookupError("introuvable")
    return {"id": user_id, "name": repo[user_id]}


for path in ("/health", "/users/1", "/users/9", "/nowhere"):
    status, body = app.dispatch(path)
    print(f"GET {path} -> {status} {body}")

app.dependency_overrides[get_repo] = lambda: {1: "Fake"}
print("Avec dépendance remplacée (test):", app.dispatch("/users/1")[1])
