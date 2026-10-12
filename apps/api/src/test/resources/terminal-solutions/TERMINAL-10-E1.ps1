New-Item -ItemType Directory -Path logs -Force | Out-Null
$log = Join-Path 'logs' 'app.log'
Set-Content -Path $log -Value @('INFO démarrage', 'ERROR disque plein', 'INFO reprise', 'ERROR timeout')

Add-Content -Path $log -Value 'INFO arrêt'
"Lignes: $((Get-Content $log).Count)"
"Erreurs: $((Select-String -Path $log -Pattern 'ERROR').Count)"
$alertes = Join-Path 'logs' 'alertes.log'
(Get-Content $log) -replace 'ERROR', 'ALERTE' | Set-Content -Path $alertes
"Alertes: $((Select-String -Path $alertes -Pattern 'ALERTE').Count)"
"Existe: $(Test-Path $log) / $(Test-Path (Join-Path 'logs' 'absent.log'))"
Copy-Item -Path $log -Destination (Join-Path 'logs' 'backup.log')
Remove-Item -Path $log
"Après suppression: $(Test-Path $log)"
