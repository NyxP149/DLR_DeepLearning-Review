#!/usr/bin/env bash
mkdir -p atelier/photos
cd atelier
touch photos/1.jpg photos/2.jpg photos/3.png photos/draft.tmp
mkdir -p trie/2024
cp photos/*.jpg trie/2024/
mv photos/3.png trie/
rm photos/draft.tmp
find . -type f | sort
