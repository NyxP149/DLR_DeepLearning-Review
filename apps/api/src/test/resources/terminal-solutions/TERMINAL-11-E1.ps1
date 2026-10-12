$sep = [IO.Path]::PathSeparator
$tools = Join-Path (Get-Location).Path 'tools'
New-Item -ItemType Directory -Path $tools -Force | Out-Null

$env:APP_ENV = 'prod'
"APP_ENV=$env:APP_ENV"
$before = ($env:PATH -split $sep).Count
$env:PATH = "$tools$sep$env:PATH"
$after = ($env:PATH -split $sep).Count
"Entrées ajoutées: $($after - $before)"
"Première entrée: $(Split-Path -Leaf ($env:PATH -split $sep)[0])"
Remove-Item Env:APP_ENV
"Supprimée: $($null -eq [Environment]::GetEnvironmentVariable('APP_ENV', 'Process'))"
