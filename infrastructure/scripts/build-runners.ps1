$ErrorActionPreference = 'Stop'

$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($null -eq $docker) {
    $userDocker = Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'
    if (-not (Test-Path -LiteralPath $userDocker)) {
        throw 'Docker CLI introuvable. Démarre Docker Desktop ou configure DLR_DOCKER_CLI.'
    }
    $dockerPath = $userDocker
}
else {
    $dockerPath = $docker.Source
}

$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '../..')
$javaRunner = Join-Path $repositoryRoot 'runners/java-runner'

& $dockerPath build --tag 'dlr/java-runner:21' $javaRunner
if ($LASTEXITCODE -ne 0) {
    throw 'La construction de dlr/java-runner:21 a échoué.'
}

Write-Host 'Image dlr/java-runner:21 construite avec succès.'

