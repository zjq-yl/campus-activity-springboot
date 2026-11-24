$ErrorActionPreference = 'Stop'
$tomcat = Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot '..\.tools') -Directory -Filter 'apache-tomcat-9*' |
  Sort-Object Name -Descending | Select-Object -First 1
if (-not $tomcat) { throw 'Tomcat 9 was not found under .tools.' }

$config = Join-Path $tomcat.FullName 'conf\logging.properties'
$content = [System.IO.File]::ReadAllText($config, [System.Text.Encoding]::UTF8)
$updated = [regex]::Replace($content, '(?m)^(java\.util\.logging\.ConsoleHandler\.encoding\s*=\s*).+$', '${1}GBK')
if ($updated -eq $content -and $content -notmatch 'java\.util\.logging\.ConsoleHandler\.encoding\s*=\s*GBK') {
  throw 'ConsoleHandler encoding setting was not found.'
}
[System.IO.File]::WriteAllText($config, $updated, (New-Object System.Text.UTF8Encoding($false)))
Write-Output 'Tomcat console logging now uses GBK; file logs remain UTF-8. Restart Tomcat to apply.'
