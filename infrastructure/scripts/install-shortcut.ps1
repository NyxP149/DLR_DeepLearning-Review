<#
.SYNOPSIS
  Crée sur le Bureau un raccourci « DLR » vers DLR.cmd (chemin calculé depuis l'emplacement du dépôt).
#>
$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$desktop = [Environment]::GetFolderPath('Desktop')
$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut((Join-Path $desktop 'DLR.lnk'))
$shortcut.TargetPath = Join-Path $repositoryRoot 'DLR.cmd'
$shortcut.WorkingDirectory = $repositoryRoot
$shortcut.Description = 'Démarrer DLR (API, base, site)'
$shortcut.Save()
Write-Host "Raccourci créé : $(Join-Path $desktop 'DLR.lnk')" -ForegroundColor Green
