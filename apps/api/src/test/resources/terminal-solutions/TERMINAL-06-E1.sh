#!/usr/bin/env bash
mkdir -p bin
printf '#!/usr/bin/env bash\necho bonjour\n' > bin/hello
chmod +x bin/hello

GREETING=salut
LOCAL=prive
export GREETING
echo "Exporté: $(bash -c 'echo $GREETING')"
value="$(bash -c 'echo $LOCAL')"
echo "Non exporté: ${value:-(vide)}"
export PATH="$PWD/bin:$PATH"
echo "Commande: $(command -v hello)"
hello
NOM="Ada Lovelace"
touch "$NOM.txt"
echo "Nom: $(ls *.txt)"
