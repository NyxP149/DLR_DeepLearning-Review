file = {"key": "exports/42.pdf", "owner": 7, "sha256": "a1b2", "size": 2048}
url_ttl_seconds = 300
print("Objet:", file["key"])
print("Métadonnées:", file["sha256"], file["size"])
print("URL temporaire:", url_ttl_seconds, "s")
