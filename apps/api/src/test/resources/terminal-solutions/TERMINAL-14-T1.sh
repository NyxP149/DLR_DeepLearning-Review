#!/usr/bin/env bash
mkdir -p rapport
cd rapport
printf 'equipe,heures,type\nweb,5,bug\napi,3,feature\nweb,2,bug\ndata,8,feature\napi,4,bug\n' > tickets.csv
echo '{"items":[{"h":3},{"h":4}]}' > resume.json
echo "Tickets par equipe: $(tail -n +2 tickets.csv | cut -d, -f1 | sort | uniq -c | awk '{printf "%s=%s ", $2, $1}' | sed 's/ $//')"
echo "Heures par equipe: $(awk -F, 'NR>1 {h[$1]+=$2} END {for (k in h) print k"="h[k]}' tickets.csv | sort | paste -sd' ')"
echo "Types distincts: $(tail -n +2 tickets.csv | cut -d, -f3 | sort -u | paste -sd' ')"
echo "Total JSON: $(jq '[.items[].h] | add' resume.json)"
echo "Renommes: $(sed 's/bug/defaut/' tickets.csv | grep -c defaut)"
