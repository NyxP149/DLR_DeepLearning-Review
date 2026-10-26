# Conservé pour compatibilité : le lancement complet est maintenant dans dlr.ps1 (voir DLR.cmd à la racine).
param(
    [Parameter(Mandatory = $true)]
    [string]$FrontendOrigin,
    [int]$ApiPort = 8081,
    [string]$DatabaseUrl = '',
    [string]$DatabaseUser = ''
)

# Comme avant : sans -DatabaseUrl, PostgreSQL local (la base du fichier dlr.config.json n'est pas reprise).
$forwarded = @{
    Mode = 'hybrid'; FrontendOrigin = $FrontendOrigin; ApiPort = $ApiPort
    DatabaseUrl = $DatabaseUrl; DatabaseUser = $DatabaseUser
}
& (Join-Path $PSScriptRoot 'dlr.ps1') start @forwarded
