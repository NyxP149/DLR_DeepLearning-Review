function ConvertTo-Duration {
    [CmdletBinding()]
    param([Parameter(Mandatory)][int]$Seconds)
    if ($Seconds -lt 0) { throw 'secondes negatives' }
    $culture = [cultureinfo]::InvariantCulture
    if ($Seconds -lt 60) { return "$Seconds s" }
    if ($Seconds -lt 3600) { return ([string]::Format($culture, '{0:0.0}', $Seconds / 60)) + ' min' }
    return ([string]::Format($culture, '{0:0.0}', $Seconds / 3600)) + ' h'
}

foreach ($s in 45, 150, 7200, -1) {
    try { Write-Output (ConvertTo-Duration -Seconds $s) } catch { Write-Output "Erreur: $($_.Exception.Message)" }
}
