import hashlib
import hmac

SECRET = b"cle-de-demo"
uploads = [
    ("photo.png", 150_000),
    ("../../etc/passwd", 10),
    ("film.mp4", 900_000),
    ("scan.pdf", 5_000_000),
]


def check(name, size):
    if "/" in name or ".." in name:
        return "refuse (nom invalide)"
    if name.rsplit(".", 1)[-1].lower() not in {"png", "jpg", "pdf"}:
        return "refuse (type non autorise)"
    if size > 2_000_000:
        return "refuse (fichier trop gros)"
    return "accepte"


def storage_key(owner, content):
    return f"files/{owner}/{hashlib.sha256(content).hexdigest()[:8]}"


def sign(path, expires):
    return hmac.new(SECRET, f"{path}|{expires}".encode(), hashlib.sha256).hexdigest()[:16]


def verify(path, expires, signature, now):
    if not hmac.compare_digest(sign(path, expires), signature):
        return "signature invalide"
    return "expiree" if now > expires else "valide"


for name, size in uploads:
    print(f"{name}: {check(name, size)}")
print("Cle:", storage_key(7, b"contenu"))
path = "/files/7/abcd1234"
signature = sign(path, 1000)
print("URL valide:", verify(path, 1000, signature, 900))
print("URL alteree:", verify(path + "x", 1000, signature, 900))
print("URL expiree:", verify(path, 1000, signature, 1001))
