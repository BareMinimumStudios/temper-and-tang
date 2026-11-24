param([switch]$Clean, [string]$Version = "")
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repoRoot
try {
    $buildArgs = @("check", "build", "--console=plain")
    if ($Clean) { $buildArgs = @("clean") + $buildArgs }
    if ($Version) {
        if ($Version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(?:[-+][A-Za-z0-9.-]+)?$') { throw "Use a semantic version." }
        $buildArgs += "-Pmod_version=$Version"
    }
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed ($LASTEXITCODE)." }
    & python scripts/verify_resources.py
    if ($LASTEXITCODE -ne 0) { throw "Resource verification failed." }
    & python scripts/verify_models.py
    if ($LASTEXITCODE -ne 0) { throw "Model verification failed." }
    & python scripts/verify_artifacts.py
    if ($LASTEXITCODE -ne 0) { throw "Artifact verification failed." }
    & python scripts/prepare_release.py
    if ($LASTEXITCODE -ne 0) { throw "Release staging failed." }
} finally { Pop-Location }
