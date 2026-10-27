<#
.SYNOPSIS
  Lance, arrête ou contrôle DLR sur ce PC (mode hybride Render + Tailscale, ou mode local complet).

.EXAMPLE
  .\infrastructure\scripts\dlr.ps1            # démarre selon dlr.config.json
  .\infrastructure\scripts\dlr.ps1 stop       # arrête l'API, Tailscale Serve et PostgreSQL local
  .\infrastructure\scripts\dlr.ps1 status
  .\infrastructure\scripts\dlr.ps1 backup   # sauvegarde la base Neon dans backups/ (mot de passe demandé)
#>
param(
    [ValidateSet('start', 'stop', 'status', 'backup')]
    [string]$Action = 'start',
    [ValidateSet('hybrid', 'local')]
    [string]$Mode,
    [string]$FrontendOrigin,
    [int]$ApiPort,
    [string]$DatabaseUrl,
    [string]$DatabaseUser,
    [switch]$Rebuild,
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$configPath = Join-Path $repositoryRoot 'dlr.config.json'

function Write-Step($text) { Write-Host "-> $text" -ForegroundColor Cyan }

function Find-Tool($name, $fallbacks, $help) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($null -ne $command) { return $command.Source }
    foreach ($fallback in $fallbacks) {
        if (Test-Path -LiteralPath $fallback) { return $fallback }
    }
    throw $help
}

# --- Configuration : fichier local (jamais suivi par Git, sans mot de passe) puis paramètres ---------------------------
$config = [ordered]@{ mode = 'hybrid'; apiPort = 8081; frontendOrigin = ''; databaseUrl = ''; databaseUser = ''; backupOnStop = $true; keepBackups = 10 }
$configExists = Test-Path -LiteralPath $configPath
if ($configExists) {
    $stored = Get-Content -LiteralPath $configPath -Raw | ConvertFrom-Json
    foreach ($key in @($config.Keys)) {
        if ($null -ne $stored.$key -and "$($stored.$key)" -ne '') { $config[$key] = $stored.$key }
    }
}
if ($PSBoundParameters.ContainsKey('Mode')) { $config.mode = $Mode }
if ($PSBoundParameters.ContainsKey('ApiPort')) { $config.apiPort = $ApiPort }
if ($PSBoundParameters.ContainsKey('FrontendOrigin')) { $config.frontendOrigin = $FrontendOrigin }
if ($PSBoundParameters.ContainsKey('DatabaseUrl')) { $config.databaseUrl = $DatabaseUrl }
if ($PSBoundParameters.ContainsKey('DatabaseUser')) { $config.databaseUser = $DatabaseUser }
$apiPort = [int]$config.apiPort

function Get-ApiProcess {
    $listener = Get-NetTCPConnection -LocalPort $apiPort -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $listener) { return $null }
    return Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
}

function Get-DockerPath {
    return Find-Tool 'docker' @((Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe')) `
        'Docker CLI introuvable. Démarre Docker Desktop puis relance.'
}

function Get-TailscalePath {
    return Find-Tool 'tailscale' @('C:\Program Files\Tailscale\tailscale.exe') `
        'Tailscale est introuvable. Installe-le et connecte cette machine à ton tailnet.'
}

function Get-ComposeArguments {
    # DLR_COMPOSE_PROJECT sert aux essais : sans lui, le projet (donc le volume de données) reste celui d'origine.
    $arguments = @()
    if (-not [string]::IsNullOrWhiteSpace($env:DLR_COMPOSE_PROJECT)) { $arguments += @('-p', $env:DLR_COMPOSE_PROJECT) }
    return $arguments + @('--file', (Join-Path $repositoryRoot 'compose.yaml'))
}

# --- sauvegarde Neon ------------------------------------------------------------------------------------------------------
# pg_dump tourne dans un conteneur postgres:17 (aucun outil PostgreSQL à installer). L'hôte « -pooler » de Neon est
# remplacé par l'hôte direct, que pg_dump exige. Le mot de passe passe par l'environnement du conteneur, jamais par un fichier.
function Backup-Database {
    param([string]$JdbcUrl, [string]$User, [string]$Password)
    $match = [regex]::Match($JdbcUrl, '^jdbc:postgresql://(?<host>[^:/?]+)(:(?<port>\d+))?/(?<db>[^?]+)(\?(?<query>.*))?$')
    if (-not $match.Success) { Write-Warning "Sauvegarde ignorée : URL JDBC illisible."; return }
    $dbHost = $match.Groups['host'].Value -replace '-pooler', ''
    $dbPort = if ($match.Groups['port'].Success) { $match.Groups['port'].Value } else { '5432' }
    $sslMode = [regex]::Match($match.Groups['query'].Value, 'sslmode=(?<mode>[a-z-]+)').Groups['mode'].Value
    $backupDirectory = Join-Path $repositoryRoot 'backups'
    New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
    $fileName = 'neon-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.dump'
    $dockerPath = Get-DockerPath
    $dockerArguments = @('run', '--rm', '-e', 'PGPASSWORD', '-v', "${backupDirectory}:/backup")
    if ($sslMode) { $dockerArguments += @('-e', "PGSSLMODE=$sslMode") }
    $dockerArguments += @('postgres:17-alpine', 'pg_dump', '--host', $dbHost, '--port', $dbPort, '--username', $User,
        '--dbname', $match.Groups['db'].Value, '--format=custom', '--no-owner', '--file', "/backup/$fileName")
    Write-Step "Sauvegarde de la base ($dbHost)"
    $env:PGPASSWORD = $Password
    try {
        & $dockerPath @dockerArguments
        $succeeded = $LASTEXITCODE -eq 0
    }
    finally { Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue }
    $file = Join-Path $backupDirectory $fileName
    if (-not $succeeded -or -not (Test-Path -LiteralPath $file)) {
        Remove-Item -LiteralPath $file -ErrorAction SilentlyContinue
        Write-Warning 'La sauvegarde a échoué (voir le message ci-dessus) : la base n''a pas été modifiée.'
        return
    }
    Write-Host ("Sauvegarde créée : backups\$fileName ({0:N0} Ko)" -f ((Get-Item -LiteralPath $file).Length / 1KB)) -ForegroundColor Green
    # Conserve les `keepBackups` plus récentes ; les sauvegardes locales (dlr-*.dump) ne sont jamais touchées.
    Get-ChildItem -LiteralPath $backupDirectory -Filter 'neon-*.dump' | Sort-Object LastWriteTime -Descending |
        Select-Object -Skip ([int]$config.keepBackups) | Remove-Item -Force
}

function Read-DatabasePassword {
    if (-not [string]::IsNullOrWhiteSpace($env:DLR_DB_PASSWORD)) { return $env:DLR_DB_PASSWORD }
    $securePassword = Read-Host 'Mot de passe de la base' -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
}

if ($Action -eq 'backup') {
    if ([string]::IsNullOrWhiteSpace($config.databaseUrl)) { throw 'Aucune base distante configurée (databaseUrl). Pour PostgreSQL local, utilise backup-database.ps1.' }
    Backup-Database -JdbcUrl $config.databaseUrl -User $config.databaseUser -Password (Read-DatabasePassword)
    return
}

# --- status ---------------------------------------------------------------------------------------------------------------
if ($Action -eq 'status') {
    $api = Get-ApiProcess
    if ($null -ne $api) {
        try {
            $health = Invoke-RestMethod -Uri "http://127.0.0.1:$apiPort/actuator/health" -TimeoutSec 3
            Write-Host "API DLR : démarrée sur le port $apiPort (processus $($api.Id)), état $($health.status)." -ForegroundColor Green
        }
        catch {
            Write-Host "API DLR : le port $apiPort est occupé (processus $($api.ProcessName) $($api.Id)) mais ne répond pas à /actuator/health." -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "API DLR : arrêtée (rien n'écoute sur le port $apiPort)."
    }
    try {
        $dockerPath = Get-DockerPath
        $running = & $dockerPath compose @(Get-ComposeArguments) ps --status running --services 2>$null
        Write-Host ("PostgreSQL local : " + $(if ($running) { 'démarré' } else { 'arrêté' }))
    }
    catch { Write-Host 'Docker : indisponible.' }
    if ($config.mode -eq 'hybrid') {
        try { & (Get-TailscalePath) serve status }
        catch { Write-Host 'Tailscale : indisponible.' }
    }
    return
}

# --- stop -----------------------------------------------------------------------------------------------------------------
function Stop-All {
    $api = Get-ApiProcess
    if ($null -ne $api -and $api.ProcessName -like 'java*') {
        Write-Step "Arrêt de l'API (processus $($api.Id))"
        Stop-Process -Id $api.Id -Force
    }
    elseif ($null -ne $api) {
        Write-Warning "Le port $apiPort est utilisé par $($api.ProcessName) : laissé tel quel."
    }
    if ($config.mode -eq 'hybrid') {
        try {
            $tailscale = Get-TailscalePath
            Write-Step 'Fin de la publication Tailscale Serve'
            # Seule la publication HTTPS du port 443 (celle de DLR) est retirée ; les autres réglages Tailscale restent.
            & $tailscale serve --https=443 off *> $null
        }
        catch { }
    }
    try {
        $dockerPath = Get-DockerPath
        & $dockerPath info *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Step 'Arrêt de PostgreSQL local (les données sont conservées)'
            & $dockerPath compose @(Get-ComposeArguments) stop postgres *> $null
        }
    }
    catch { }
    Write-Host 'DLR est arrêté.' -ForegroundColor Green
}

if ($Action -eq 'stop') {
    Stop-All
    return
}

# --- start ----------------------------------------------------------------------------------------------------------------
if ($null -ne (Get-ApiProcess)) {
    throw "Quelque chose écoute déjà sur le port $apiPort. Lance `.\DLR.cmd stop` (ou `dlr.ps1 stop`) puis recommence."
}

$hybrid = $config.mode -eq 'hybrid'
if ($hybrid -and [string]::IsNullOrWhiteSpace($config.frontendOrigin)) {
    $config.frontendOrigin = Read-Host "URL du site Render (ex. https://dlr-web.onrender.com)"
}
if ($hybrid) {
    try { $origin = [System.Uri]$config.frontendOrigin.TrimEnd('/') }
    catch { throw "frontendOrigin doit être une URL HTTPS valide, par exemple https://dlr-web.onrender.com." }
    if ($origin.Scheme -ne 'https' -or $origin.AbsolutePath -ne '/') {
        throw 'frontendOrigin doit être une origine HTTPS sans chemin, par exemple https://dlr-web.onrender.com.'
    }
    $frontendUrl = $origin.GetLeftPart([System.UriPartial]::Authority)
}
# Le mode local utilise PostgreSQL local, sauf si -DatabaseUrl est donné explicitement : la base Neon du fichier de
# configuration n'est jamais touchée par un essai local.
$useNeon = -not [string]::IsNullOrWhiteSpace($config.databaseUrl) -and ($hybrid -or $PSBoundParameters.ContainsKey('DatabaseUrl'))
if ([string]::IsNullOrWhiteSpace($config.databaseUrl) -xor [string]::IsNullOrWhiteSpace($config.databaseUser)) {
    throw 'databaseUrl et databaseUser doivent être renseignés ensemble (ou tous les deux vides pour PostgreSQL local).'
}
if (-not $configExists) {
    $config | ConvertTo-Json | Set-Content -LiteralPath $configPath -Encoding UTF8
    Write-Host "Configuration enregistrée dans dlr.config.json (ignorée par Git, sans mot de passe)." -ForegroundColor DarkGray
}

Write-Step 'Vérification de Docker'
$dockerPath = Get-DockerPath
& $dockerPath info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop ne répond pas. Attends que son moteur soit prêt puis relance.' }
$tailscalePath = $null
if ($hybrid) { $tailscalePath = Get-TailscalePath }

Write-Step 'Base de données'
if ($useNeon) {
    $securePassword = Read-Host 'Mot de passe Neon' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { $env:DLR_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer) }
    $env:DLR_DB_URL = $config.databaseUrl
    $env:DLR_DB_USER = $config.databaseUser
    Write-Host 'Base Neon sélectionnée.' -ForegroundColor Green
}
else {
    $dbPort = if ($env:DLR_DB_PORT) { $env:DLR_DB_PORT } else { '5434' }
    & $dockerPath compose @(Get-ComposeArguments) up --detach --wait postgres
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL local n'a pas pu démarrer." }
    $env:DLR_DB_URL = "jdbc:postgresql://localhost:$dbPort/dlr"
    $env:DLR_DB_USER = 'dlr'
    $env:DLR_DB_PASSWORD = 'dlr-local-only'
    Write-Host 'Base PostgreSQL locale sélectionnée.' -ForegroundColor Green
}

Write-Step 'Images Runner'
$runnerImages = @('dlr/java-runner:21', 'dlr/python-runner:3.13', 'dlr/typescript-runner:22', 'dlr/shell-runner:1')
$missingRunner = $false
foreach ($runnerImage in $runnerImages) {
    & $dockerPath image inspect $runnerImage *> $null
    if ($LASTEXITCODE -ne 0) { $missingRunner = $true }
}
if ($missingRunner) {
    & (Join-Path $PSScriptRoot 'build-runners.ps1')
    if ($LASTEXITCODE -ne 0) { throw 'La construction des images Runner a échoué.' }
}

Write-Step 'Professeur IA'
if ([string]::IsNullOrWhiteSpace($env:DLR_DEEPSEEK_API_KEY) -and $env:DLR_TUTOR_PROVIDER -ne 'ollama') {
    $secureDeepSeekKey = Read-Host 'Clé API DeepSeek (laisser vide pour utiliser Ollama en local)' -AsSecureString
    if ($secureDeepSeekKey.Length -gt 0) {
        $deepSeekKeyPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureDeepSeekKey)
        try { $env:DLR_DEEPSEEK_API_KEY = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($deepSeekKeyPointer) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($deepSeekKeyPointer) }
    }
}
if ([string]::IsNullOrWhiteSpace($env:DLR_DEEPSEEK_API_KEY)) {
    $env:DLR_TUTOR_PROVIDER = 'ollama'
    Write-Host 'Professeur IA : Ollama (aucune clé DeepSeek fournie).' -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri 'http://127.0.0.1:11434/api/tags' -TimeoutSec 3 | Out-Null
        Write-Host 'Ollama détecté.' -ForegroundColor Green
    }
    catch {
        Write-Warning 'Ollama ne répond pas sur le port 11434. Le Runner fonctionnera, mais le professeur IA restera en mode dégradé.'
    }
}
else {
    $env:DLR_TUTOR_PROVIDER = 'deepseek'
    Write-Host 'Professeur IA : DeepSeek activé.' -ForegroundColor Green
}

# Le jar embarque aussi le contenu des laboratoires : il est refait si le code, le pom ou `content/` ont changé.
Write-Step "Programme de l'API"
$apiDirectory = Join-Path $repositoryRoot 'apps/api'
$jar = Get-ChildItem -LiteralPath (Join-Path $apiDirectory 'target') -Filter 'dlr-api-*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*.original' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$sources = @(
    (Get-ChildItem -LiteralPath (Join-Path $apiDirectory 'src/main') -Recurse -File),
    (Get-ChildItem -LiteralPath (Join-Path $repositoryRoot 'content') -Recurse -File),
    (Get-Item -LiteralPath (Join-Path $apiDirectory 'pom.xml'))
) | ForEach-Object { $_ }
$newestSource = ($sources | Measure-Object LastWriteTime -Maximum).Maximum
if ($Rebuild -or $null -eq $jar -or $jar.LastWriteTime -lt $newestSource) {
    Write-Host 'Construction du jar (première fois ou code modifié)...'
    $mavenRepository = Join-Path $repositoryRoot '.m2/repository'
    Push-Location $apiDirectory
    try {
        & mvn "-Dmaven.repo.local=$mavenRepository" -q -DskipTests package
        if ($LASTEXITCODE -ne 0) { throw 'La construction (mvn package) a échoué.' }
    }
    finally { Pop-Location }
    $jar = Get-ChildItem -LiteralPath (Join-Path $apiDirectory 'target') -Filter 'dlr-api-*.jar' |
        Where-Object { $_.Name -notlike '*.original' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}
else {
    Write-Host "Jar à jour ($($jar.Name)), aucune reconstruction."
}

$env:DLR_API_PORT = "$apiPort"
$env:DLR_EXECUTION_ENABLED = 'true'
$env:DLR_DOCKER_CLI = $dockerPath
if ($hybrid) {
    $tailscaleStatus = & $tailscalePath status --json | ConvertFrom-Json
    $dnsName = [string]$tailscaleStatus.Self.DNSName
    if ([string]::IsNullOrWhiteSpace($dnsName)) { throw "Cette machine n'est pas connectée à Tailscale ou MagicDNS est indisponible." }
    $tailscaleUrl = 'https://' + $dnsName.TrimEnd('.')
    Write-Step 'Publication privée via Tailscale Serve'
    & $tailscalePath serve --bg "http://127.0.0.1:$apiPort"
    if ($LASTEXITCODE -ne 0) { throw "Tailscale Serve n'a pas pu publier l'API dans ton réseau privé." }
    $env:DLR_ALLOWED_ORIGINS = "$frontendUrl,http://localhost:4200,http://127.0.0.1:4200"
    $env:DLR_FRONTEND_URL = $frontendUrl
    $env:DLR_PUBLIC_API_URL = $tailscaleUrl
}
else {
    $env:DLR_ALLOWED_ORIGINS = 'http://localhost:4200,http://127.0.0.1:4200'
}

Write-Step "Démarrage de l'API"
$java = Find-Tool 'java' @() 'Java 21 est introuvable dans le PATH.'
$apiProcess = Start-Process -FilePath $java -ArgumentList @('-jar', $jar.FullName) -WorkingDirectory $apiDirectory `
    -PassThru -NoNewWindow
$frontendProcess = $null
try {
    $ready = $false
    for ($attempt = 0; $attempt -lt 90 -and -not $apiProcess.HasExited; $attempt++) {
        try {
            $health = Invoke-RestMethod -Uri "http://127.0.0.1:$apiPort/actuator/health" -TimeoutSec 2
            if ($health.status -eq 'UP') { $ready = $true; break }
        }
        catch { }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw "L'API ne répond pas (arrêt du processus ou délai de 3 minutes dépassé). Consulte les messages ci-dessus." }

    Write-Host ''
    Write-Host "API DLR prête sur http://localhost:$apiPort." -ForegroundColor Green
    if ($hybrid) {
        Write-Host "Site : $frontendUrl" -ForegroundColor Green
        Write-Host "URL privée de l'API (DLR_HYBRID_API_BASE_URL sur Render) : $tailscaleUrl" -ForegroundColor DarkGray
        $openUrl = $frontendUrl
    }
    else {
        $webDirectory = Join-Path $repositoryRoot 'apps/web'
        if (-not (Test-Path -LiteralPath (Join-Path $webDirectory 'node_modules'))) {
            Write-Step 'Installation des dépendances du site (npm install)'
            Push-Location $webDirectory
            try { & npm install } finally { Pop-Location }
        }
        Write-Step 'Démarrage du site local'
        $frontendProcess = Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c', 'npm', 'start') -WorkingDirectory $webDirectory `
            -PassThru -WindowStyle Minimized
        $openUrl = 'http://localhost:4200'
        Write-Host "Site local : $openUrl (compilation en cours, quelques secondes)" -ForegroundColor Green
    }
    if (-not $NoBrowser) { Start-Process $openUrl }
    Write-Host 'Garde cette fenêtre ouverte. Ctrl+C ou DLR.cmd stop pour tout arrêter.' -ForegroundColor Yellow
    $apiProcess.WaitForExit()
}
finally {
    if ($null -ne $frontendProcess -and -not $frontendProcess.HasExited) {
        & taskkill /PID $frontendProcess.Id /T /F *> $null
    }
    if (-not $apiProcess.HasExited) { Stop-Process -Id $apiProcess.Id -Force }
    # L'API est arrêtée : sauvegarde cohérente de Neon avant de tout couper (un échec n'empêche pas l'arrêt).
    if ($useNeon -and $config.backupOnStop -and -not [string]::IsNullOrWhiteSpace($env:DLR_DB_PASSWORD)) {
        try { Backup-Database -JdbcUrl $config.databaseUrl -User $config.databaseUser -Password $env:DLR_DB_PASSWORD }
        catch { Write-Warning "Sauvegarde ignorée : $($_.Exception.Message)" }
    }
    Stop-All
}
