function ConvertTo-Size {
    [CmdletBinding()]
    param([Parameter(Mandatory)][long]$Bytes)
    if ($Bytes -lt 0) { throw 'octets négatifs' }
    if ($Bytes -lt 1KB) { return "$Bytes B" }
    if ($Bytes -lt 1MB) { return [string]::Format([cultureinfo]::InvariantCulture, '{0:N1} KB', $Bytes / 1KB) }
    return [string]::Format([cultureinfo]::InvariantCulture, '{0:N1} MB', $Bytes / 1MB)
}

foreach ($value in 512, 1536, 2097152, -1) {
    try {
        ConvertTo-Size -Bytes $value
    }
    catch {
        "Erreur: $($_.Exception.Message)"
    }
}
