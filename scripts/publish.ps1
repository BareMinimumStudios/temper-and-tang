[CmdletBinding()]
param(
    [ValidateSet("prepare", "modrinth", "curseforge", "all")]
    [string]$Target = "prepare",

    [ValidateSet("all", "fabric", "neoforge")]
    [string]$Loader = "all",

    [ValidateSet("alpha", "beta", "release")]
    [string]$Channel = "beta",

    [switch]$Upload
)

$ErrorActionPreference = "Stop"
if ($Target -ne "prepare" -and -not $Upload) {
    throw "Pass -Upload to dispatch a publishing run, or use -Target prepare."
}
if ($Target -eq "prepare" -and $Upload) {
    throw "Choose modrinth, curseforge, or all with -Upload."
}
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "Install GitHub CLI and sign in, or dispatch Build and publish from GitHub Actions."
}

$repository = "BareMinimumStudios/temper-and-tang"
& gh workflow run publish.yml --repo $repository --ref main -f "target=$Target" -f "loader=$Loader" -f "channel=$Channel"
if ($LASTEXITCODE -ne 0) {
    throw "GitHub did not accept the workflow dispatch."
}
Write-Host "Dispatched $Target. See https://github.com/$repository/actions/workflows/publish.yml"
