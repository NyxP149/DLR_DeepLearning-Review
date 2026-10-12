#!/usr/bin/env bash
mkdir -p atelier/src/app atelier/docs atelier/tests
touch atelier/src/app/main.txt atelier/docs/guide.txt

cd atelier
cd src/app
pwd
cd ../../docs
pwd
cd ..
pwd
echo "Contenu: $(ls | xargs)"
