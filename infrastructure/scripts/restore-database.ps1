param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

if (-not $Force) {
    throw 'La restauration remplace les données DLR existantes. Relance avec -Force après avoir vérifié le fichier.'
}

$resolvedBackup = (Resolve-Path -LiteralPath $BackupFile).Path
if ([System.IO.Path]::GetExtension($resolvedBackup) -ne '.dump') {
    throw 'Le fichier doit être une sauvegarde DLR au format .dump.'
}

$command = Get-Command docker -ErrorAction SilentlyContinue
if ($null -ne $command) {
    $dockerPath = $command.Source
}
else {
    $dockerPath = Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'
    if (-not (Test-Path -LiteralPath $dockerPath)) {
        throw 'Docker CLI introuvable. Démarre Docker Desktop avant la restauration.'
    }
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$containerId = (& $dockerPath compose --project-directory $repositoryRoot ps -q postgres).Trim()
if ([string]::IsNullOrWhiteSpace($containerId)) {
    throw "Le conteneur PostgreSQL DLR n'est pas démarré."
}

$containerBackup = '/tmp/dlr-restore.dump'
try {
    & $dockerPath cp $resolvedBackup "${containerId}:$containerBackup"
    if ($LASTEXITCODE -ne 0) { throw 'La copie de la sauvegarde a échoué.' }
    & $dockerPath exec $containerId pg_restore -U dlr -d dlr --clean --if-exists --no-owner --single-transaction $containerBackup
    if ($LASTEXITCODE -ne 0) { throw 'La restauration PostgreSQL a échoué ; la transaction a été annulée.' }
}
finally {
    & $dockerPath exec $containerId rm -f $containerBackup 2>$null
}

Write-Host "Base DLR restaurée depuis : $resolvedBackup"
