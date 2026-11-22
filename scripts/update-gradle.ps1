$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$release = Invoke-RestMethod "https://services.gradle.org/versions/current"
if (-not $release.current -or $release.snapshot -or $release.nightly -or $release.broken) { throw "Expected a stable Gradle release." }
Push-Location $repoRoot
try {
    & .\gradlew.bat wrapper --gradle-version $release.version --distribution-type bin --gradle-distribution-sha256-sum $release.checksum
    if ($LASTEXITCODE -ne 0) { throw "Wrapper upgrade failed." }
    & .\gradlew.bat wrapper
    if ($LASTEXITCODE -ne 0) { throw "Wrapper regeneration failed." }
} finally { Pop-Location }
