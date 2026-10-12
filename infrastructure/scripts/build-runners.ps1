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
$pythonRunner = Join-Path $repositoryRoot 'runners/python-runner'
$typescriptRunner = Join-Path $repositoryRoot 'runners/typescript-runner'
$shellRunner = Join-Path $repositoryRoot 'runners/shell-runner'

& $dockerPath build --tag 'dlr/java-runner:21' $javaRunner
if ($LASTEXITCODE -ne 0) {
    throw 'La construction de dlr/java-runner:21 a échoué.'
}

Write-Host 'Image dlr/java-runner:21 construite avec succès.'

& $dockerPath build --tag 'dlr/python-runner:3.13' $pythonRunner
if ($LASTEXITCODE -ne 0) { throw 'La construction de dlr/python-runner:3.13 a échoué.' }

& $dockerPath build --tag 'dlr/typescript-runner:22' $typescriptRunner
if ($LASTEXITCODE -ne 0) { throw 'La construction de dlr/typescript-runner:22 a échoué.' }

& $dockerPath build --tag 'dlr/shell-runner:1' $shellRunner
if ($LASTEXITCODE -ne 0) { throw 'La construction de dlr/shell-runner:1 a échoué.' }

Write-Host 'Images Java, Python, TypeScript et Shell (bash + PowerShell) construites avec succès.'
