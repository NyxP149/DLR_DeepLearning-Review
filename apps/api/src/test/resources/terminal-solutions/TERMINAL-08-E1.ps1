New-Item -ItemType Directory -Path atelier/docs -Force | Out-Null
New-Item -ItemType File -Path atelier/docs/guide.txt -Force | Out-Null
New-Item -ItemType File -Path atelier/readme.txt -Force | Out-Null

Set-Location atelier
"Dossier: $(Split-Path -Leaf (Get-Location).Path)"
"Contenu: $((Get-ChildItem | Sort-Object Name | ForEach-Object Name) -join ', ')"
"gci = $((Get-Alias gci).Definition)"
"Type: $((Get-Command Get-ChildItem).CommandType)"
