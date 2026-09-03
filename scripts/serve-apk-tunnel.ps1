#Requires -Version 5.1
param(
    [int]$Port = 8765,
    [string]$ApkName = "",
    [string]$ApkDir = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not $ApkDir) {
    $ApkDir = Join-Path $Root "apk-dist"
}

function Find-Cloudflared {
    $cmd = Get-Command cloudflared -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $candidates = @(
        "$env:ProgramFiles\cloudflared\cloudflared.exe",
        "$env:LOCALAPPDATA\Microsoft\WinGet\Links\cloudflared.exe",
        "$env:LOCALAPPDATA\cloudflared\cloudflared.exe"
    )
    foreach ($p in $candidates) {
        if (Test-Path $p) { return $p }
    }
    return $null
}

if (-not (Test-Path $ApkDir)) {
    New-Item -ItemType Directory -Path $ApkDir | Out-Null
}

$apks = @(Get-ChildItem -Path $ApkDir -Filter "*.apk" -File -ErrorAction SilentlyContinue)
if ($apks.Count -eq 0) {
    Write-Host "ERROR: no .apk in $ApkDir" -ForegroundColor Red
    Write-Host "Copy release APK first, e.g. apk-dist\smart-emap.apk"
    exit 1
}

if ($ApkName) {
    $apk = Join-Path $ApkDir $ApkName
    if (-not (Test-Path $apk)) {
        Write-Host "ERROR: missing $apk" -ForegroundColor Red
        exit 1
    }
} else {
    $picked = $apks | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    $ApkName = $picked.Name
    $apk = $picked.FullName
}

$cloudflared = Find-Cloudflared
if (-not $cloudflared) {
    Write-Host "ERROR: cloudflared not found. Install: winget install Cloudflare.cloudflared" -ForegroundColor Red
    exit 1
}

$sha = Get-FileHash -Path $apk -Algorithm SHA256
$bytes = [byte[]]::new($sha.Hash.Length / 2)
for ($i = 0; $i -lt $bytes.Length; $i++) {
    $bytes[$i] = [Convert]::ToByte($sha.Hash.Substring($i * 2, 2), 16)
}
$pkgChecksum = [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")

Write-Host ""
Write-Host "=== Smart-EMAP APK Tunnel ===" -ForegroundColor Cyan
Write-Host "APK      : $apk"
Write-Host "Local    : http://127.0.0.1:$Port/$ApkName"
Write-Host "PACKAGE_CHECKSUM: $pkgChecksum"
Write-Host ""

$py = $null
foreach ($c in @("py", "python")) {
    $cmd = Get-Command $c -ErrorAction SilentlyContinue
    if ($cmd) { $py = $cmd.Source; break }
}
if (-not $py) {
    Write-Host "ERROR: Python not found (need py -3)" -ForegroundColor Red
    exit 1
}

$httpJob = Start-Job -ScriptBlock {
    param($pyExe, $dir, $port)
    Set-Location $dir
    if ($pyExe -like "*\py.exe") {
        & $pyExe -3 -m http.server $port --bind 127.0.0.1
    } else {
        & $pyExe -m http.server $port --bind 127.0.0.1
    }
} -ArgumentList $py, $ApkDir, $Port

Start-Sleep -Seconds 2
if ($httpJob.State -ne "Running") {
    $err = Receive-Job $httpJob 2>&1 | Out-String
    Write-Host "ERROR: local HTTP failed on port $Port" -ForegroundColor Red
    Write-Host $err
    exit 1
}

$logOut = Join-Path $env:TEMP "smart-emap-cloudflared.out.log"
$logErr = Join-Path $env:TEMP "smart-emap-cloudflared.err.log"
foreach ($f in @($logOut, $logErr)) {
    if (Test-Path $f) { Remove-Item $f -Force }
}

$cfProc = Start-Process -FilePath $cloudflared `
    -ArgumentList @("tunnel", "--url", "http://127.0.0.1:$Port", "--no-autoupdate") `
    -RedirectStandardOutput $logOut `
    -RedirectStandardError $logErr `
    -PassThru `
    -WindowStyle Hidden

$publicBase = $null
for ($i = 0; $i -lt 90; $i++) {
    Start-Sleep -Seconds 1
    $text = ""
    if (Test-Path $logOut) { $text += (Get-Content $logOut -Raw -ErrorAction SilentlyContinue) }
    if (Test-Path $logErr) { $text += (Get-Content $logErr -Raw -ErrorAction SilentlyContinue) }
    if ($text -match "https://[a-z0-9-]+\.trycloudflare\.com") {
        $publicBase = $Matches[0]
        break
    }
    if ($cfProc.HasExited) { break }
}

if (-not $publicBase) {
    Write-Host "ERROR: could not parse trycloudflare.com URL from cloudflared log" -ForegroundColor Red
    if (Test-Path $logOut) { Get-Content $logOut }
    if (Test-Path $logErr) { Get-Content $logErr }
    Stop-Job $httpJob -ErrorAction SilentlyContinue
    Remove-Job $httpJob -Force -ErrorAction SilentlyContinue
    if (-not $cfProc.HasExited) { Stop-Process -Id $cfProc.Id -Force -ErrorAction SilentlyContinue }
    exit 1
}

$publicApk = "$publicBase/$ApkName"
$qrJsonPath = Join-Path $ApkDir "provisioning-qr.json"
$pretty = @"
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.example.smart_emap/com.example.smart_emap.admin.SmartEmapDeviceAdminReceiver",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "$publicApk",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM": "$pkgChecksum",
  "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": true,
  "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": true
}
"@
Set-Content -Path $qrJsonPath -Value $pretty -Encoding UTF8

Write-Host ""
Write-Host "=== Public APK URL (for QR) ===" -ForegroundColor Green
Write-Host $publicApk -ForegroundColor Green
Write-Host ""
Write-Host "QR JSON written: $qrJsonPath" -ForegroundColor Cyan
Write-Host "Keep this window open until tablets finish provisioning." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow
Write-Host ""

try {
    while (-not $cfProc.HasExited) {
        Start-Sleep -Seconds 2
    }
} finally {
    if (-not $cfProc.HasExited) {
        Stop-Process -Id $cfProc.Id -Force -ErrorAction SilentlyContinue
    }
    Stop-Job $httpJob -ErrorAction SilentlyContinue
    Remove-Job $httpJob -Force -ErrorAction SilentlyContinue
    Write-Host "Stopped local server and tunnel."
}
