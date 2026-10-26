New-Item -ItemType Directory -Path vrac -Force | Out-Null
foreach ($name in 'a.txt', 'b.txt', 'c.csv', 'run.sh', 'README') { New-Item -ItemType File -Path (Join-Path 'vrac' $name) -Force | Out-Null }
$files = Get-ChildItem vrac -File
$groups = $files | Group-Object { if ($_.Extension) { $_.Extension } else { '(aucune)' } }
$ordered = $groups | Sort-Object { if ($_.Name -eq '(aucune)') { '~' } else { $_.Name } }
Write-Output ("Extensions: " + (($ordered | ForEach-Object { "$($_.Name)=$($_.Count)" }) -join ', '))
Write-Output "Fichiers: $($files.Count)"
$separator = [IO.Path]::DirectorySeparatorChar
Write-Output "Chemin portable: $((Join-Path 'vrac' 'a.txt') -eq ('vrac' + $separator + 'a.txt'))"
Write-Output 'Equivalences:'
$map = @{ 'cat' = 'Get-Content'; 'cd' = 'Set-Location'; 'grep' = 'Select-String' }
foreach ($key in ($map.Keys | Sort-Object)) { Write-Output "$key -> $($map[$key])" }
