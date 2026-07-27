param (
    [Parameter(Mandatory=$true, HelpMessage="Email address for the new admin")]
    [string]$Email,

    [Parameter(Mandatory=$true, HelpMessage="Password for the new admin")]
    [string]$Password
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectDir = Join-Path $ScriptDir ".."

Set-Location $ProjectDir

Write-Host "🚀 Starting EventHub Admin Creation Tool..." -ForegroundColor Cyan
Write-Host "This will briefly start the Spring Boot context to create the admin in the database." -ForegroundColor Cyan

# Run maven wrapper and pass the args. We disable the web server so it runs quickly and avoids port conflicts.
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--add-admin=$Email`:$Password --spring.main.web-application-type=none" -q

Write-Host "✅ Done." -ForegroundColor Green
