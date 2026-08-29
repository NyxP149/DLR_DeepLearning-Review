param(
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'

function Resolve-DlrDocker {
    $command = Get-Command docker -ErrorAction SilentlyContinue
    if ($null -ne $command) { return $command.Source }
    $desktopCli = Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'
    if (Test-Path -LiteralPath $desktopCli) { return $desktopCli }
    throw 'Docker CLI introuvable. Démarre Docker Desktop avant la sauvegarde.'
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $repositoryRoot 'backups'
}
$resolvedOutput = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $resolvedOutput -Force | Out-Null

$dockerPath = Resolve-DlrDocker
$containerId = (& $dockerPath compose --project-directory $repositoryRoot ps -q postgres).Trim()
if ([string]::IsNullOrWhiteSpace($containerId)) {
    throw "Le conteneur PostgreSQL DLR n'est pas démarré."
}

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$containerBackup = "/tmp/dlr-$timestamp.dump"
$destination = Join-Path $resolvedOutput "dlr-$timestamp.dump"

try {
    & $dockerPath exec $containerId pg_dump -U dlr -d dlr -Fc -f $containerBackup
    if ($LASTEXITCODE -ne 0) { throw 'pg_dump a échoué.' }
    & $dockerPath cp "${containerId}:$containerBackup" $destination
    if ($LASTEXITCODE -ne 0) { throw 'La copie de la sauvegarde a échoué.' }
}
finally {
    & $dockerPath exec $containerId rm -f $containerBackup 2>$null
}

$size = (Get-Item -LiteralPath $destination).Length
Write-Host "Sauvegarde DLR créée : $destination ($size octets)"
