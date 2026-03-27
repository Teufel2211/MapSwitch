param(
    [Parameter(Mandatory = $true)]
    [string]$Version,
    [string]$SinceTag = "",
    [string]$ChangelogPath = "CHANGELOG.md"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-CommitLines {
    param([string]$FromTag)
    if ([string]::IsNullOrWhiteSpace($FromTag)) {
        return git log --pretty=format:"%s"
    }
    return git log "$FromTag..HEAD" --pretty=format:"%s"
}

function Group-Commit {
    param([string]$Line)
    $lower = $Line.ToLowerInvariant()
    if ($lower.StartsWith("feat")) { return "Features" }
    if ($lower.StartsWith("fix")) { return "Fixes" }
    if ($lower.StartsWith("perf") -or $lower.StartsWith("refactor") -or $lower.StartsWith("docs")) { return "Improvements" }
    return "Internal"
}

if (!(Test-Path -LiteralPath $ChangelogPath)) {
    throw "Changelog file not found: $ChangelogPath"
}

$commits = Get-CommitLines -FromTag $SinceTag | Where-Object { $_ -and $_.Trim().Length -gt 0 }
if ($commits.Count -eq 0) {
    Write-Host "No commits found for release notes."
    exit 0
}

$grouped = @{
    Features = New-Object System.Collections.Generic.List[string]
    Improvements = New-Object System.Collections.Generic.List[string]
    Fixes = New-Object System.Collections.Generic.List[string]
    Internal = New-Object System.Collections.Generic.List[string]
}

foreach ($c in $commits) {
    $section = Group-Commit -Line $c
    $grouped[$section].Add($c)
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("## $Version")
foreach ($section in @("Features", "Improvements", "Fixes", "Internal")) {
    $lines.Add("### $section")
    if ($grouped[$section].Count -eq 0) {
        $lines.Add("- (none)")
    } else {
        foreach ($entry in $grouped[$section]) {
            $lines.Add("- $entry")
        }
    }
}
$lines.Add("")

$current = Get-Content -LiteralPath $ChangelogPath -Raw
$updated = $current.TrimEnd() + "`r`n`r`n" + ($lines -join "`r`n")
Set-Content -LiteralPath $ChangelogPath -Value $updated -NoNewline

Write-Host "Appended release notes for $Version to $ChangelogPath"
