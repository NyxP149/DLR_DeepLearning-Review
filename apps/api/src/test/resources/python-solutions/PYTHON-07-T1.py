import importlib
from pathlib import Path

package = Path("inventory")
package.mkdir(exist_ok=True)
(package / "stock.py").write_text(
    "def count_items(items):\n"
    "    return len(items)\n",
    encoding="utf-8",
)
(package / "__init__.py").write_text(
    "from .stock import count_items\n\n__all__ = ['count_items']\n",
    encoding="utf-8",
)

importlib.invalidate_caches()
inventory = importlib.import_module("inventory")
print("Nom du paquet:", inventory.__name__)
print("Exports:", inventory.__all__)
print("count_items:", inventory.count_items(["a", "b", "c"]))
print("Module d'origine:", inventory.count_items.__module__)
