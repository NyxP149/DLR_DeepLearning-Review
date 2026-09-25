#!/usr/bin/env bash
set -euo pipefail

cd "/c/Users/sanyx/Documents/GitHub/DLR_DeepLearning-Review"

powershell.exe -ExecutionPolicy Bypass -File "./infrastructure/scripts/start-hybrid.ps1" \
  -FrontendOrigin "https://dlr-web.onrender.com" \
  -DatabaseUrl "jdbc:postgresql://ep-noisy-night-zatekj2n-pooler.c-2.eu-west-2.aws.neon.tech/neondb?sslmode=require" \
  -DatabaseUser "neondb_owner"
