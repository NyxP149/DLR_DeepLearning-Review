param(
    [Parameter(Mandatory = $true)]
    [string]$FrontendOrigin,
    [int]$ApiPort = 8081,
    [string]$DatabaseUrl = '',
    [string]$DatabaseUser = ''
)

$ErrorActionPreference = 'Stop'

try {
    $origin = [System.Uri]$FrontendOrigin.TrimEnd('/')
}
catch {
    throw 'FrontendOrigin doit être une URL HTTPS valide, par exemple https://dlr-web.onrender.com.'
}
if ($origin.Scheme -ne 'https' -or $origin.AbsolutePath -ne '/') {
    throw 'FrontendOrigin doit être une origine HTTPS sans chemin, par exemple https://dlr-web.onrender.com.'
}
$frontendUrl = $origin.GetLeftPart([System.UriPartial]::Authority)

$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($null -eq $docker) {
    $userDocker = Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'
    if (-not (Test-Path -LiteralPath $userDocker)) {
        throw 'Docker CLI introuvable. Démarre Docker Desktop puis relance ce script.'
    }
    $dockerPath = $userDocker
}
else {
    $dockerPath = $docker.Source
}

$tailscale = Get-Command tailscale -ErrorAction SilentlyContinue
if ($null -eq $tailscale) {
    $tailscaleInstall = 'C:\Program Files\Tailscale\tailscale.exe'
    if (-not (Test-Path -LiteralPath $tailscaleInstall)) {
        throw 'Tailscale est introuvable. Installe-le et connecte cette machine à ton tailnet.'
    }
    $tailscalePath = $tailscaleInstall
}
else {
    $tailscalePath = $tailscale.Source
}

& $dockerPath info | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Desktop ne répond pas. Attends que son moteur soit prêt puis relance le script.'
}

$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '../..')
if ([string]::IsNullOrWhiteSpace($DatabaseUrl) -xor [string]::IsNullOrWhiteSpace($DatabaseUser)) {
    throw 'DatabaseUrl et DatabaseUser doivent être fournis ensemble.'
}
if (-not [string]::IsNullOrWhiteSpace($DatabaseUrl)) {
    $securePassword = Read-Host 'Mot de passe Neon' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:DLR_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    $env:DLR_DB_URL = $DatabaseUrl
    $env:DLR_DB_USER = $DatabaseUser
    Write-Host 'Base Neon sélectionnée.' -ForegroundColor Green
}
else {
    & $dockerPath compose --file (Join-Path $repositoryRoot 'compose.yaml') up --detach postgres
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL local n'a pas pu démarrer."
    }
    Write-Host 'Base PostgreSQL locale sélectionnée.' -ForegroundColor Green
}

$runnerImages = @('dlr/java-runner:21', 'dlr/python-runner:3.13', 'dlr/typescript-runner:22')
$missingRunner = $false
foreach ($runnerImage in $runnerImages) {
    & $dockerPath image inspect $runnerImage *> $null
    if ($LASTEXITCODE -ne 0) { $missingRunner = $true }
}
if ($missingRunner) {
    & (Join-Path $PSScriptRoot 'build-runners.ps1')
    if ($LASTEXITCODE -ne 0) { throw 'La construction des images Runner a échoué.' }
}

try {
    Invoke-RestMethod -Uri 'http://127.0.0.1:11434/api/tags' -TimeoutSec 3 | Out-Null
    Write-Host 'Ollama détecté.' -ForegroundColor Green
}
catch {
    Write-Warning 'Ollama ne répond pas sur le port 11434. Le Runner fonctionnera, mais le professeur IA restera en mode dégradé.'
}

$tailscaleStatus = & $tailscalePath status --json | ConvertFrom-Json
$dnsName = [string]$tailscaleStatus.Self.DNSName
if ([string]::IsNullOrWhiteSpace($dnsName)) {
    throw "Cette machine n'est pas connectée à Tailscale ou MagicDNS est indisponible."
}
$tailscaleUrl = 'https://' + $dnsName.TrimEnd('.')

& $tailscalePath serve --bg "http://127.0.0.1:$ApiPort"
if ($LASTEXITCODE -ne 0) {
    throw "Tailscale Serve n'a pas pu publier l'API dans ton réseau privé."
}

$env:DLR_API_PORT = "$ApiPort"
$env:DLR_ALLOWED_ORIGINS = "$frontendUrl,http://localhost:4200,http://127.0.0.1:4200"
$env:DLR_FRONTEND_URL = $frontendUrl
$env:DLR_PUBLIC_API_URL = $tailscaleUrl
$env:DLR_EXECUTION_ENABLED = 'true'
$env:DLR_DOCKER_CLI = $dockerPath

Write-Host ''
Write-Host "Adresse privée à placer dans DLR_HYBRID_API_BASE_URL sur Render : $tailscaleUrl" -ForegroundColor Cyan
& $tailscalePath serve status
Write-Host ''
Write-Host "L'API DLR démarre au premier plan. Garde cette fenêtre ouverte." -ForegroundColor Green

$apiDirectory = Join-Path $repositoryRoot 'apps/api'
$mavenRepository = Join-Path $repositoryRoot '.m2/repository'
Push-Location $apiDirectory
try {
    & mvn "-Dmaven.repo.local=$mavenRepository" spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw "L'API DLR s'est arrêtée avec une erreur." }
}
finally {
    Pop-Location
}
