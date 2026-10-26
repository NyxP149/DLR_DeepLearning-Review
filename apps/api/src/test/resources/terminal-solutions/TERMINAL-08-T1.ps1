New-Item -ItemType Directory -Path site/css -Force | Out-Null
Set-Content site/index.html '<h1>Accueil</h1>'
Set-Content site/css/style.css 'body{}'
Set-Location site
Write-Output "Dossier: $(Split-Path (Get-Location).Path -Leaf)"
Write-Output ("Contenu: " + ((Get-ChildItem | Sort-Object Name).Name -join ', '))
Write-Output "gcm = $((Get-Alias gcm).Definition)"
Write-Output "Type: $((Get-Command Get-Content).CommandType)"
