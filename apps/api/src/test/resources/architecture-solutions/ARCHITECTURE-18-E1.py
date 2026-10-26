import hashlib
import hmac

SECRET = b"secret-de-test"
ALLOWED_TYPES = {"application/pdf", "image/png"}
MAX_SIZE = 5_000_000
TTL = 300


def validate(name: str, content_type: str, size: int) -> str | None:
    if content_type not in ALLOWED_TYPES:
        return "type non autorisé"
    if size > MAX_SIZE:
        return "fichier trop gros"
    if "/" in name or "\\" in name or ".." in name:
        return "nom invalide"
    return None


def storage_key(owner: int, content: bytes) -> str:
    return f"files/{owner}/{hashlib.sha256(content).hexdigest()[:8]}"


def sign(key: str, expires: int) -> str:
    return hmac.new(SECRET, f"{key}:{expires}".encode("utf-8"), hashlib.sha256).hexdigest()


def verify(key: str, expires: int, signature: str, now: int) -> str:
    if now > expires:
        return "expirée"
    if not hmac.compare_digest(signature, sign(key, expires)):
        return "signature invalide"
    return "valide"


uploads = [("rapport.pdf", "application/pdf", 2048), ("../../etc/passwd", "application/pdf", 10),
           ("film.mp4", "video/mp4", 1000), ("gros.png", "image/png", 9_000_000)]
for name, content_type, size in uploads:
    reason = validate(name, content_type, size)
    print(f"{name}: {'accepté' if reason is None else f'refusé ({reason})'}")
key = storage_key(7, b"contenu du fichier")
now = 1000
expires = now + TTL
signature = sign(key, expires)
print(f"Clé: {key}")
print(f"URL temporaire: {TTL} s")
print(f"Contrôle à t=1100: {verify(key, expires, signature, 1100)}")
print(f"Contrôle à t=1400: {verify(key, expires, signature, 1400)}")
print(f"Clé altérée: {verify('files/8/abcd1234', expires, signature, 1100)}")
