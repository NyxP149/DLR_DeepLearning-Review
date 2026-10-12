New-Item -ItemType Directory -Path vrac -Force | Out-Null
foreach ($name in 'a.txt', 'b.txt', 'c.csv', 'd.sh', 'LISEZMOI') {
    New-Item -ItemType File -Path (Join-Path 'vrac' $name) -Force | Out-Null
}

$equivalences = @{ 'ls' = 'Get-ChildItem'; 'cat' = 'Get-Content'; 'grep' = 'Select-String'; 'rm' = 'Remove-Item'; 'cd' = 'Set-Location' }

$files = Get-ChildItem vrac
$groups = $files | Group-Object Extension | Sort-Object { if ($_.Name -eq '') { 'zzz' } else { $_.Name } } |
    ForEach-Object { $label = if ($_.Name -eq '') { '(aucune)' } else { $_.Name }; "$label=$($_.Count)" }
"Extensions: $($groups -join ', ')"
"Fichiers: $(($files | Measure-Object).Count)"
$sep = [IO.Path]::DirectorySeparatorChar
"Chemin portable: $((Join-Path 'vrac' 'a.txt') -eq "vrac${sep}a.txt")"
"Équivalences:"
foreach ($key in ($equivalences.Keys | Sort-Object)) { "$key -> $($equivalences[$key])" }
