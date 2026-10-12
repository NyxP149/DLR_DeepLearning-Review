New-Item -ItemType Directory -Path data -Force | Out-Null
$sizes = @{ 'a.txt' = 10; 'b.txt' = 20; 'report.csv' = 300; 'data.csv' = 50; 'data.json' = 200 }
foreach ($name in $sizes.Keys) {
    Set-Content -Path "data/$name" -Value ('x' * $sizes[$name]) -NoNewline
}
$files = Get-ChildItem data

$big = $files | Where-Object Length -gt 100 | Sort-Object Length -Descending | ForEach-Object Name
"Gros fichiers: $($big -join ', ')"
"Total octets: $(($files | Measure-Object -Property Length -Sum).Sum)"
$groups = $files | Group-Object Extension | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Count)" }
"Par extension: $($groups -join ', ')"
$smallest = $files | Sort-Object Length | Select-Object -First 1
"Plus petit: $($smallest.Name) ($($smallest.Length))"
