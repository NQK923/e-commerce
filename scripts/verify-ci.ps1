param(
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$SkipDockerConfig,
    [switch]$InstallFrontendDependencies
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $repoRoot "e-commerce-frontend"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Name"
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Step failed: $Name"
    }
}

Push-Location $repoRoot
try {
    if (-not $SkipDockerConfig) {
        Invoke-Step "Validate Docker Compose config" {
            docker compose config --quiet
        }
    }

    if (-not $SkipBackend) {
        Invoke-Step "Backend build and tests" {
            .\gradlew.bat build --console=plain
        }
    }

    if (-not $SkipFrontend) {
        Push-Location $frontendDir
        try {
            if ($InstallFrontendDependencies) {
                Invoke-Step "Install frontend dependencies" {
                    npm ci
                }
            }

            Invoke-Step "Frontend lint" {
                npm run lint
            }

            Invoke-Step "Frontend production build" {
                npm run build
            }

            Invoke-Step "Frontend e2e tests" {
                npx playwright test
            }
        } finally {
            Pop-Location
        }
    }

    Write-Host ""
    Write-Host "Verification completed successfully."
} finally {
    Pop-Location
}
