param([switch]$Demo)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $taskMavenRepository = Join-Path $env:USERPROFILE '.m2\repository'
    $taskArguments = @('-B', "-Dmaven.repo.local=$taskMavenRepository", 'compile', 'exec:java')
    if ($Demo) { $taskArguments += '-Dexec.args=--demo' }
    & mvn @taskArguments
    if ($LASTEXITCODE -ne 0) { throw 'Maven khong chay thanh cong. Xem thong bao phia tren.' }
} finally {
    Pop-Location
}
