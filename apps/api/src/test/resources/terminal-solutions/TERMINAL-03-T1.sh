#!/usr/bin/env bash
mkdir -p projet/old
cd projet
printf 'INFO  demarrage\nWARN  disque presque plein\nERROR connexion refusee\nINFO  reprise\nFATAL arret force\n' > app.log
printf 'INFO  ancien\n' > old/app-2023.log
echo "Erreurs: $(grep -c -E 'ERROR|FATAL' app.log)"
echo "Premier WARN: ligne $(grep -n WARN app.log | head -1 | cut -d: -f1)"
echo "Dernieres lignes:"
tail -n 2 app.log
echo "Fichiers log: $(find . -name '*.log' | sort | tr '\n' ' ' | sed 's/ $//')"
