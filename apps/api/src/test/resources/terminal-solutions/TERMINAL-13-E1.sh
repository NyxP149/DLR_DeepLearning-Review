#!/usr/bin/env bash
set -euo pipefail
cleanup() { echo "Nettoyage effectué"; }
trap cleanup EXIT

sauvegarder() {
  if [[ $# -ne 1 ]]; then
    echo "usage: sauvegarder <dossier>" >&2
    return 2
  fi
  if [[ ! -d "$1" ]]; then
    echo "dossier introuvable: $1" >&2
    return 3
  fi
  local n
  n=$(find "$1" -type f | wc -l)
  mkdir -p sauvegarde && cp -r "$1"/. sauvegarde/
  echo "Sauvegarde: $n fichiers"
}

mkdir -p projet/src
touch projet/a.txt projet/b.txt projet/src/c.txt

sauvegarder 2>/dev/null || echo "Usage refusé: code $?"
sauvegarder absent 2>/dev/null || echo "Dossier absent: code $?"
sauvegarder projet
