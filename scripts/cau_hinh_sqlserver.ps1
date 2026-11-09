param(
    [string]$MayChu = '.\SQLEXPRESS',
    [string]$DiaChiJdbc = 'localhost',
    [ValidateRange(1, 65535)][int]$Cong = 1433
)
$ErrorActionPreference = 'Stop'
$taskProject = Split-Path $PSScriptRoot -Parent
$taskConfig = Join-Path $taskProject 'db.properties'
if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) {
    throw 'Can sqlcmd. Hoac chay DATA/nhakhoa_sqlserver.sql bang SSMS va tu tao db.properties theo file example.'
}
if (Test-Path -LiteralPath $taskConfig) {
    throw 'db.properties da ton tai. Giu nguyen cau hinh hien tai; neu can nap du lieu, chay DATA/nhakhoa_sqlserver.sql bang SSMS.'
}
# Windows Authentication cua nguoi dung hien tai phai co quyen tao database/login.
& sqlcmd -S $MayChu -E -C -b -r 1 -f 65001 -i (Join-Path $taskProject 'DATA\nhakhoa_sqlserver.sql')
if ($LASTEXITCODE -ne 0) { throw 'Khong khoi tao duoc database. Kiem tra quyen Windows Authentication tren SQL Server.' }

# Tao login SQL rieng cho ung dung, khong su dung hoac thay doi tai khoan sa.
$taskLogin = 'n15_nhakhoa_' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
$taskPassword = 'N15!aA' + [Guid]::NewGuid().ToString('N')
$taskAccountSql = @"
SET NOCOUNT ON;
IF CAST(SERVERPROPERTY('IsIntegratedSecurityOnly') AS int)=1
    THROW 51000,'SQL Server can bat Mixed Mode (SQL Server and Windows Authentication).',1;
CREATE LOGIN [$taskLogin] WITH PASSWORD=N'$taskPassword', CHECK_POLICY=ON;
USE N15_NHAKHOA_DB;
CREATE USER [$taskLogin] FOR LOGIN [$taskLogin];
ALTER ROLE db_datareader ADD MEMBER [$taskLogin];
ALTER ROLE db_datawriter ADD MEMBER [$taskLogin];
"@
& sqlcmd -S $MayChu -E -C -b -r 1 -Q $taskAccountSql
if ($LASTEXITCODE -ne 0) { throw 'Khong tao duoc login SQL cho ung dung.' }
$taskContent = @"
# Cau hinh cuc bo, duoc .gitignore bo qua. Khong chia se mat khau nay.
db.url=jdbc:sqlserver://${DiaChiJdbc}:${Cong};databaseName=N15_NHAKHOA_DB;encrypt=true;trustServerCertificate=true
db.user=$taskLogin
db.password=$taskPassword
"@
[System.IO.File]::WriteAllText($taskConfig, $taskContent, [System.Text.UTF8Encoding]::new($false))
Write-Output 'Da tao N15_NHAKHOA_DB va db.properties. Chay .\run.ps1 hoac chay_bai_thanh.cmd de mo ung dung.'
