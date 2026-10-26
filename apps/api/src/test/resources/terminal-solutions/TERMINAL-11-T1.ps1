New-Item -ItemType Directory -Path tools -Force | Out-Null
$env:LAYER = 'staging'
Write-Output "LAYER=$env:LAYER"
$sep = [IO.Path]::PathSeparator
$before = $env:PATH -split [regex]::Escape($sep)
$env:PATH = (Resolve-Path tools).Path + $sep + $env:PATH
$after = $env:PATH -split [regex]::Escape($sep)
Write-Output "Entrees ajoutees: $($after.Count - $before.Count)"
Write-Output "Premiere entree: $(Split-Path $after[0] -Leaf)"
Remove-Item Env:LAYER
Write-Output "Supprimee: $(($null -eq $env:LAYER) -and ($null -eq [Environment]::GetEnvironmentVariable('LAYER')))"
