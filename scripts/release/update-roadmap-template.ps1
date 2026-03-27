param(
    [string]$ChangelogPath = "CHANGELOG.md",
    [string]$OutputPath = "docs/github-project-template.md"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $ChangelogPath)) {
    throw "Changelog not found: $ChangelogPath"
}

$content = Get-Content -LiteralPath $ChangelogPath -Raw
$matches = [regex]::Matches($content, "(?ms)^##\s+(\d+\.\d+\.\d+)\r?\n(.*?)(?=^##\s+\d+\.\d+\.\d+|\z)")

$pendingVersions = New-Object System.Collections.Generic.List[string]
foreach ($m in $matches) {
    $version = $m.Groups[1].Value
    $body = $m.Groups[2].Value
    if ($body -match "\(pending\)") {
        $pendingVersions.Add($version)
    }
}

$milestones = $pendingVersions | Select-Object -First 5
if ($milestones.Count -eq 0) {
    $milestones = @("1.0.2", "1.0.3", "1.0.4")
}

$starterIssues = @(
    "Add per-map scoreboard/locator customization",
    "Add configurable teleport target per map",
    "Add optional permission toggle (public vs restricted commands)",
    "Add data migration command for old map folders",
    "Improve diagnostics command (`/mapswitch debug`)"
)

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("# GitHub Project Template (MapSwitch)")
$lines.Add("")
$lines.Add("Nutze ein Board mit folgenden Spalten:")
$lines.Add("- Backlog")
$lines.Add("- Ready")
$lines.Add("- In Progress")
$lines.Add("- Review")
$lines.Add("- Done")
$lines.Add("")
$lines.Add("## Empfohlene Labels")
$lines.Add('- `feature`')
$lines.Add('- `bug`')
$lines.Add('- `docs`')
$lines.Add('- `release`')
$lines.Add('- `infra`')
$lines.Add('- `good first issue`')
$lines.Add("")
$lines.Add("## Roadmap (Auto)")
$lines.Add("Automatisch aus `CHANGELOG.md` abgeleitet am $(Get-Date -Format "yyyy-MM-dd HH:mm").")
$lines.Add("")
foreach ($v in $milestones) {
    $lines.Add("### Milestone $v")
    $lines.Add("- Status: Planned")
    $lines.Add("- Ziel: Alle `(pending)` Punkte fuer diese Version aus dem Changelog ersetzen.")
    $lines.Add("")
}

$lines.Add("## Starter-Issues")
for ($i = 0; $i -lt $starterIssues.Count; $i++) {
    $lines.Add("$($i + 1). $($starterIssues[$i]).")
}

$text = ($lines -join "`r`n") + "`r`n"
Set-Content -LiteralPath $OutputPath -Value $text -NoNewline
Write-Host "Updated $OutputPath"
