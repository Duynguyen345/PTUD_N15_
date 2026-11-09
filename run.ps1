param()
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath 'db.properties')) {
        throw 'Chua co db.properties. Chay DATA/nhakhoa_sqlserver.sql, sau do sao chep src/main/resources/db.properties.example thanh db.properties va dien thong tin SQL Server.'
    }
    $taskMaven = Get-Command mvn -ErrorAction SilentlyContinue
    $taskMavenPath = if ($taskMaven) { $taskMaven.Source } else {
        Join-Path $env:APPDATA 'Code\User\globalStorage\pleiades.java-extension-pack-jdk\maven\latest\bin\mvn.cmd'
    }
    if (-not (Test-Path -LiteralPath $taskMavenPath)) {
        throw 'Khong tim thay Maven. Cai Maven va JDK 21+ de chay ung dung SQL Server.'
    }
    $taskMavenRepository = Join-Path $env:USERPROFILE '.m2\repository'
    $taskArguments = @('-B', "-Dmaven.repo.local=$taskMavenRepository", 'compile', 'exec:java')
    & $taskMavenPath @taskArguments
    if ($LASTEXITCODE -ne 0) { throw 'Maven khong chay thanh cong. Xem thong bao phia tren.' }
} finally {
    Pop-Location
}
