param(
    [string]$Entry = 'probe.CatchProbe',
    [string]$Output = 'catch-before',
    [ValidateSet('SIMPLE', 'ADVANCED', 'FULL')][string]$Optimization = 'SIMPLE',
    [switch]$Patched
)
$ErrorActionPreference = 'Stop'
$wasmProject = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $wasmProject
$diagnosticCp = ((Get-Content cp.txt -Raw).Trim().Split(';') |
    Where-Object { $_ -notmatch '-sources.jar$|-javadoc.jar$|teavm-extras-slf4j' }) -join ';'
$classlibDependencies = @(
    'C:\Users\15937\.gradle\caches\modules-2\files-2.1\com.jcraft\jzlib\1.1.3\c01428efa717624f7aabf4df319939dda9646b2d\jzlib-1.1.3.jar',
    'C:\Users\15937\.gradle\caches\modules-2\files-2.1\joda-time\joda-time\2.12.2\78e18a7b4180e911dafba0a412adfa82c1e3d14b\joda-time-2.12.2.jar'
)
$diagnosticCp += ';' + ($classlibDependencies -join ';')
$probeCp = "diagnosis\classes;build\classes\java\main;build\resources\main;$diagnosticCp"
if ($Patched) { $probeCp = "diagnosis\patch-classes;$probeCp" }
java -Xmx1g -cp $probeCp BuildProbe $Entry "diagnosis\$Output" $Optimization
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
node test\validate.mjs "diagnosis\$Output\probe.wasm"
exit $LASTEXITCODE
