param(
  [ValidateSet('catalina', 'localhost')]
  [string]$Name = 'catalina',
  [int]$Tail = 80
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$tools = Join-Path $PSScriptRoot '..\.tools'
$tomcat = Get-ChildItem -LiteralPath $tools -Directory -Filter 'apache-tomcat-9*' |
  Sort-Object Name -Descending | Select-Object -First 1
if (-not $tomcat) { throw 'Tomcat 9 was not found under .tools.' }

$log = Get-ChildItem -LiteralPath (Join-Path $tomcat.FullName 'logs') -Filter "$Name.*.log" |
  Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $log) { throw "No $Name log was found." }

Get-Content -LiteralPath $log.FullName -Encoding UTF8 -Tail $Tail
