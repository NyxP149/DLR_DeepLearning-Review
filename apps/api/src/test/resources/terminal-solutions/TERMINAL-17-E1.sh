#!/usr/bin/env bash
mkdir -p src
echo "contenu" > src/app.txt

cat > Makefile <<'MAKE'
.RECIPEPREFIX = >
.PHONY: all test clean
all: dist/app.txt
dist/app.txt: src/app.txt
> mkdir -p dist
> cp src/app.txt dist/app.txt
> echo construit
test: dist/app.txt
> grep -q contenu dist/app.txt && echo "test ok"
clean:
> rm -rf dist
MAKE

make -s all 2>/dev/null || true
make -q all 2>/dev/null && echo "À jour" || echo "À reconstruire"
touch -d 'now + 2 seconds' src/app.txt
make -q all 2>/dev/null && echo "À jour" || echo "À reconstruire"
make -s test 2>/dev/null || true
make -s clean 2>/dev/null || true
[ -d dist ] && echo "dist présent" || echo "dist supprimé"
