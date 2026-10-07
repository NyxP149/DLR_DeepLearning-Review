import importlib
from pathlib import Path

package = Path("shop")
package.mkdir(exist_ok=True)

(package / "text.py").write_text(
    "def slugify(value: str) -> str:\n"
    "    return value.strip().lower().replace(' ', '-')\n",
    encoding="utf-8",
)
(package / "__init__.py").write_text(
    "from .text import slugify\n\n__all__ = ['slugify']\n",
    encoding="utf-8",
)

importlib.invalidate_caches()
shop = importlib.import_module("shop")
slugify = getattr(shop, "slugify", None)

print(f"Nom du paquet: {shop.__name__}")
print(f"Exports: {getattr(shop, '__all__', None)}")
print(f"slugify: {slugify('Hello DLR Python') if slugify else 'absent'}")
print(f"Module d'origine: {slugify.__module__ if slugify else 'absent'}")
