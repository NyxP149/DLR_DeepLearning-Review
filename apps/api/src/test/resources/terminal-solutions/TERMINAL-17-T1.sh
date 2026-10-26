#!/usr/bin/env bash
mkdir -p proj/src
cd proj
echo contenu > src/page.txt
cat > Makefile <<'EOF'
.RECIPEPREFIX = >
.PHONY: all test clean
all: dist/page.txt
dist/page.txt: src/page.txt
> mkdir -p dist
> cp src/page.txt dist/page.txt
> echo construit
test: all
> grep -q contenu dist/page.txt
> echo test ok
clean:
> rm -rf dist
> echo dist supprime
EOF
make -s
make -q dist/page.txt && echo "A jour"
touch -d "2 minutes ago" dist/page.txt
make -q dist/page.txt || echo "A reconstruire"
make -s
make -s test
make -s clean
