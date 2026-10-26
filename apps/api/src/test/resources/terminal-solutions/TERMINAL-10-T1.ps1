New-Item -ItemType Directory -Path audit -Force | Out-Null
$log = 'audit/audit.log'
Set-Content $log @('INFO start', 'FAIL disk', 'INFO retry', 'FAIL network')
Add-Content $log 'INFO done'
$lines = Get-Content $log
Write-Output "Lignes: $($lines.Count)"
Write-Output "Echecs: $((Select-String -Path $log -Pattern 'FAIL').Count)"
$copy = 'audit/audit-fr.log'
(Get-Content $log) -replace 'FAIL', 'ECHEC' | Set-Content $copy
Write-Output "Echecs traduits: $((Select-String -Path $copy -Pattern 'ECHEC').Count)"
Write-Output "Existe: $(Test-Path $log) / $(Test-Path (Join-Path 'audit' 'absent.log'))"
Remove-Item $copy
Write-Output "Apres suppression: $(Test-Path $copy)"
