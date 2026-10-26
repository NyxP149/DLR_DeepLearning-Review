#!/usr/bin/env bash
mkdir -p dep
cd dep
printf '#!/usr/bin/env bash\necho livraison ok\n' > release.sh
echo cle > key.txt
chmod 644 release.sh key.txt
echo "Avant: $(stat -c %a release.sh)"
./release.sh 2>/dev/null
echo "Execution refusee: code $?"
chmod 750 release.sh
echo "Apres: $(stat -c %a release.sh)"
echo "Resultat: $(./release.sh)"
chmod 600 key.txt
echo "Cle: $(stat -c %a key.txt)"
