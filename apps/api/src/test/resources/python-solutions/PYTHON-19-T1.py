import html
import os

BASE = "/srv/uploads"


def resolve(user_path):
    full = os.path.normpath(os.path.join(BASE, user_path))
    if not full.startswith(BASE + os.sep):
        raise ValueError("sortie du dossier")
    return full


def mask(secret):
    return "****" + secret[-4:]


for name in ("../secret.txt", "photo.png"):
    try:
        resolve(name)
        print(f"{name}: accepte")
    except ValueError as error:
        print(f"{name}: refuse ({error})")
text = "<script>alert(1)</script>"
print(f"{text} -> {html.escape(text)}")
print("Cle masquee:", mask("sk-live-abcd9xyz"))
