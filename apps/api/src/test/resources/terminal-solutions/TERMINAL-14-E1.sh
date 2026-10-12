#!/usr/bin/env bash
cat > ventes.csv <<'CSV'
pays,produit,montant
FR,clavier,50
DE,souris,20
FR,souris,20
FR,ecran,200
DE,clavier,50
CSV
cat > client.json <<'JSON'
{"nom":"Ada","commandes":[{"id":1,"total":120},{"id":2,"total":80}]}
JSON

echo "Lignes par pays: $(tail -n +2 ventes.csv | cut -d, -f1 | sort | uniq -c | awk '{print $2"="$1}' | xargs)"
echo "Total par pays: $(awk -F, 'NR>1 {t[$1]+=$3} END {for (p in t) print p"="t[p]}' ventes.csv | sort | xargs)"
echo "Produits distincts: $(tail -n +2 ventes.csv | cut -d, -f2 | sort -u | xargs)"
echo "Total JSON: $(jq '[.commandes[].total] | add' client.json)"
echo "Renommés: $(sed 's/souris/mouse/' ventes.csv | grep -c mouse)"
