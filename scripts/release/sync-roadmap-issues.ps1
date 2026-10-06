param(
    [string]$Repo = "Teufel2211/MapSwitch",
    [string]$ChangelogPath = "CHANGELOG.md",
    [string[]]$Versions = @("1.0.2", "1.0.3", "1.1.0"),
    [string[]]$RequestedLabels = @("roadmap", "release"),
    [bool]$CloseStaleRoadmapIssues = $true,
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Require-Token {
    if (![string]::IsNullOrWhiteSpace($env:GITHUB_TOKEN)) {
        return
    }

    $gh = Get-Command gh -ErrorAction SilentlyContinue
    if ($null -ne $gh) {
        try {
            $ghToken = (& gh auth token 2>$null).Trim()
            if (![string]::IsNullOrWhiteSpace($ghToken)) {
                $env:GITHUB_TOKEN = $ghToken
                return
            }
        } catch {
            # Fall through to explicit error below.
        }
    }

    throw "GITHUB_TOKEN is not set. Set `$env:GITHUB_TOKEN='...' or login via `gh auth login`."
}

function Invoke-GitHubJson {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null
    )
    $headers = @{
        Authorization = "Bearer $env:GITHUB_TOKEN"
        Accept = "application/vnd.github+json"
        "X-GitHub-Api-Version" = "2022-11-28"
    }

    if ($null -eq $Body) {
        return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers
    }
    $json = $Body | ConvertTo-Json -Depth 10
    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers -Body $json -ContentType "application/json"
}

function Parse-ChangelogByVersion {
    param([string]$Path)
    $raw = Get-Content -LiteralPath $Path -Raw
    $matches = [regex]::Matches($raw, "(?ms)^##\s+(\d+\.\d+\.\d+)\r?\n(.*?)(?=^##\s+\d+\.\d+\.\d+|\z)")
    $map = @{}
    foreach ($m in $matches) {
        $map[$m.Groups[1].Value] = $m.Groups[2].Value.Trim()
    }
    return $map
}

function Build-IssueBody {
    param(
        [string]$Version,
        [string]$Section
    )
    return @"
## Roadmap Milestone $Version

Diese Issue wird aus `CHANGELOG.md` synchronisiert.

### Geplanter Umfang
$Section

### Checklist
- [ ] Feature-Umfang final abstimmen
- [ ] Umsetzung in PRs aufteilen
- [ ] Tests / Server-Testlauf
- [ ] Doku + Changelog finalisieren
"@
}

function Get-MilestoneVersionFromTitle {
    param([string]$Title)
    $m = [regex]::Match($Title, "^Milestone (\d+\.\d+\.\d+)$")
    if ($m.Success) {
        return $m.Groups[1].Value
    }
    return $null
}

function Is-SyncedRoadmapIssue {
    param([object]$Issue)
    $hasPullRequestProperty = $Issue.PSObject.Properties.Name -contains "pull_request"
    if ($hasPullRequestProperty -and $null -ne $Issue.pull_request) {
        return $false
    }
    $hasTitleProperty = $Issue.PSObject.Properties.Name -contains "title"
    if (-not $hasTitleProperty) {
        return $false
    }
    $version = Get-MilestoneVersionFromTitle -Title $Issue.title
    if ([string]::IsNullOrWhiteSpace($version)) {
        return $false
    }
    $hasBodyProperty = $Issue.PSObject.Properties.Name -contains "body"
    if (-not $hasBodyProperty -or [string]::IsNullOrWhiteSpace($Issue.body)) {
        return $false
    }
    return ($Issue.body -like "*Diese Issue wird aus `CHANGELOG.md` synchronisiert.*")
}

Require-Token

if (!(Test-Path -LiteralPath $ChangelogPath)) {
    throw "Changelog not found: $ChangelogPath"
}

$sections = Parse-ChangelogByVersion -Path $ChangelogPath

$base = "https://api.github.com/repos/$Repo"
$allIssues = Invoke-GitHubJson -Method GET -Url "$base/issues?state=all&per_page=100"
$allLabels = Invoke-GitHubJson -Method GET -Url "$base/labels?per_page=100"
$usableLabels = @($RequestedLabels | Where-Object { $allLabels.name -contains $_ })
$knownVersions = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($v in $sections.Keys) {
    [void]$knownVersions.Add($v)
}

foreach ($version in $Versions) {
    if (-not $sections.ContainsKey($version)) {
        Write-Warning "Version $version not found in changelog. Skipping."
        continue
    }

    $title = "Milestone $version"
    $body = Build-IssueBody -Version $version -Section $sections[$version]
    $existing = $allIssues | Where-Object { $_.title -eq $title } | Select-Object -First 1

    $payload = @{
        title = $title
        body = $body
        labels = $usableLabels
        state = "open"
    }

    if ($existing) {
        if ($DryRun) {
            Write-Host "[DRY-RUN] Would update issue #$($existing.number): $title"
        } else {
            Invoke-GitHubJson -Method PATCH -Url "$base/issues/$($existing.number)" -Body $payload | Out-Null
            Write-Host "Updated issue #$($existing.number): $title"
        }
    } else {
        if ($DryRun) {
            Write-Host "[DRY-RUN] Would create issue: $title"
        } else {
            $created = Invoke-GitHubJson -Method POST -Url "$base/issues" -Body $payload
            Write-Host "Created issue #$($created.number): $title"
        }
    }
}

if ($CloseStaleRoadmapIssues) {
    foreach ($issue in ($allIssues | Where-Object { Is-SyncedRoadmapIssue -Issue $_ })) {
        $issueVersion = Get-MilestoneVersionFromTitle -Title $issue.title
        if ($knownVersions.Contains($issueVersion)) {
            continue
        }
        if ($issue.state -eq "closed") {
            continue
        }

        $closePayload = @{ state = "closed" }
        if ($DryRun) {
            Write-Host "[DRY-RUN] Would close stale issue #$($issue.number): $($issue.title)"
        } else {
            Invoke-GitHubJson -Method PATCH -Url "$base/issues/$($issue.number)" -Body $closePayload | Out-Null
            Write-Host "Closed stale issue #$($issue.number): $($issue.title)"
        }
    }
}
