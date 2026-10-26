#!/usr/bin/env bash
mkdir -p lab/tools
cd lab
printf '#!/usr/bin/env bash\necho salut depuis hello\n' > tools/hello
chmod +x tools/hello
MODE=demo
EXPORTED=visible
export EXPORTED
echo "Exporte: $(bash -c 'echo $EXPORTED')"
value="$(bash -c 'echo $MODE')"
echo "Non exporte: ${value:-(vide)}"
export PATH="$PWD/tools:$PATH"
echo "Commande: $(command -v hello)"
hello
NAME="Grace Hopper"
touch "$NAME.txt"
echo "Nom: $(ls *.txt)"
