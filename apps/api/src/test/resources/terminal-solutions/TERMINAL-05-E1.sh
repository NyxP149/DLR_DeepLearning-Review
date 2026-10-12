#!/usr/bin/env bash
umask 022
printf '#!/usr/bin/env bash\necho "déploiement ok"\n' > deploy.sh
echo "mot-de-passe" > secret.txt

echo "Avant: $(stat -c %a deploy.sh)"
./deploy.sh 2>/dev/null
echo "Exécution refusée: code $?"
chmod 755 deploy.sh
echo "Après: $(stat -c %a deploy.sh)"
echo "Résultat: $(./deploy.sh)"
chmod 600 secret.txt
echo "Secret: $(stat -c %a secret.txt)"
