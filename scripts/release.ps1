param(
    [ValidateSet("release", "beta", "alpha")]
    [string]$Channel = "release",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-Checked {
    param([string]$FilePath, [string[]]$Arguments, [string]$Message)
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Message Exit code: $LASTEXITCODE" }
}

function Get-Output {
    param([string[]]$Arguments, [string]$Message)
    $value = & git @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "$Message`n$($value | Out-String)" }
    return ($value | Out-String).Trim()
}

function Read-Properties([string]$Path) {
    $result = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        $text = $line.Trim()
        if (-not $text -or $text.StartsWith("#")) { continue }
        $index = $text.IndexOf("=")
        if ($index -gt 0) { $result[$text.Substring(0, $index).Trim()] = $text.Substring($index + 1).Trim() }
    }
    return $result
}

function Require([hashtable]$Properties, [string]$Name) {
    if (-not $Properties.ContainsKey($Name) -or [string]::IsNullOrWhiteSpace($Properties[$Name])) {
        throw "gradle.properties does not define '$Name'."
    }
    return [string]$Properties[$Name]
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw "Git is not available in PATH." }
$root = [IO.Path]::GetFullPath((Get-Output @("rev-parse", "--show-toplevel") "Not inside a Git repository."))
if ([IO.Path]::GetFullPath((Get-Location).Path).TrimEnd("\") -ne $root.TrimEnd("\")) {
    throw "Run this script from the repository root: $root"
}

$properties = Read-Properties (Join-Path $root "gradle.properties")
$modVersion = Require $properties "mod_version"
$minecraftVersion = Require $properties "minecraft_version"
$uApiRange = Require $properties "u_api_version_range"
$modName = Require $properties "mod_name"
$remote = if ($properties.ContainsKey("release_remote")) { $properties["release_remote"] } else { "github" }
$branch = Get-Output @("branch", "--show-current") "Cannot determine current branch."
if (-not $branch) { throw "Detached HEAD cannot be released." }
if (Get-Output @("status", "--porcelain=v1", "--untracked-files=all") "Cannot inspect working tree.") {
    throw "Working tree is not clean. Commit or stash changes before releasing."
}
if ($modVersion -notmatch '^\d+\.\d+\.\d+(?:-[0-9A-Za-z][0-9A-Za-z.-]*)?$') { throw "Invalid mod_version: $modVersion" }

Invoke-Checked git @("fetch", $remote, $branch, "--tags", "--prune") "git fetch failed."
$localHead = Get-Output @("rev-parse", "HEAD") "Cannot read HEAD."
$remoteHead = Get-Output @("rev-parse", "$remote/$branch") "Remote branch '$remote/$branch' is missing."
if ($localHead -ne $remoteHead) { throw "Local HEAD does not match $remote/$branch. Push or pull first." }

$suffix = if ($Channel -eq "release") { "" } else { "-$Channel" }
$version = "$modVersion$suffix+mc$minecraftVersion"
$tag = "v$version"
& git show-ref --tags --verify --quiet "refs/tags/$tag"
if ($LASTEXITCODE -eq 0) { throw "Tag '$tag' already exists." }

Write-Host "Project: $modName"
Write-Host "Branch: $branch"
Write-Host "Version: $version"
Write-Host "Required U-API: $uApiRange"
Write-Host "Running clean build before creating $tag..."
Invoke-Checked (Join-Path $root "gradlew.bat") @("--no-daemon", "clean", "build") "Build failed; no tag was created."

if ($DryRun) {
    Write-Host "Dry run completed. No tag was created."
    exit 0
}

$created = $false
try {
    Invoke-Checked git @("tag", "-a", $tag, "-m", "$modName $version") "Cannot create tag."
    $created = $true
    Invoke-Checked git @("push", $remote, $tag) "Cannot push tag."
    Write-Host "Release tag pushed: $tag"
} catch {
    if ($created) { & git tag -d $tag *> $null }
    throw
}
