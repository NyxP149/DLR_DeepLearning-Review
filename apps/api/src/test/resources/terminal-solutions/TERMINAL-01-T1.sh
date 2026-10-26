#!/usr/bin/env bash
mkdir -p site/pages/blog site/assets site/tests
cd site
cd pages/blog
pwd
cd ../../assets
pwd
cd ..
pwd
echo "Contenu: $(ls | tr '\n' ' ' | sed 's/ $//')"
