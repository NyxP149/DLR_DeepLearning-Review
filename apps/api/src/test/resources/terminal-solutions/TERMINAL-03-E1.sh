#!/usr/bin/env bash
mkdir -p old
printf '%s\n' 'INFO  démarrage' 'WARN  disque à 85%' 'ERROR connexion refusée' 'INFO  reprise' 'ERROR timeout' > app.log
printf '%s\n' 'INFO  archive 2023' > old/app-2023.log

echo "Erreurs: $(grep -c ERROR app.log)"
echo "Premier WARN: ligne $(grep -n WARN app.log | head -1 | cut -d: -f1)"
echo "Dernières lignes:"
tail -n 2 app.log
echo "Fichiers log: $(find . -name '*.log' | sort | xargs)"
