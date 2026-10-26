New-Item -ItemType Directory -Path logs -Force | Out-Null
Set-Content logs/a.log ('x' * 30) -NoNewline
Set-Content logs/b.log ('x' * 250) -NoNewline
Set-Content logs/c.txt ('x' * 120) -NoNewline
Set-Content logs/d.txt ('x' * 10) -NoNewline
Set-Content logs/e.log ('x' * 400) -NoNewline
$files = Get-ChildItem logs -File
$big = $files | Where-Object Length -gt 100 | Sort-Object Length -Descending
Write-Output ("Gros fichiers: " + ($big.Name -join ', '))
Write-Output ("Total octets: " + ($files | Measure-Object Length -Sum).Sum)
$groups = $files | Group-Object Extension | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Count)" }
Write-Output ("Par extension: " + ($groups -join ', '))
$small = $files | Sort-Object Length | Select-Object -First 1
Write-Output "Plus petit: $($small.Name) ($($small.Length))"
