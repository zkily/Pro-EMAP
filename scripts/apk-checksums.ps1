#Requires -Version 5.1
param(
    [Parameter(Mandatory = $true)]
    [string]$ApkPath
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path $ApkPath)) {
    Write-Host "ERROR: APK not found: $ApkPath" -ForegroundColor Red
    exit 1
}

function To-UrlSafeBase64([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
}

$sha = Get-FileHash -Path $ApkPath -Algorithm SHA256
$pkgBytes = [byte[]]::new($sha.Hash.Length / 2)
for ($i = 0; $i -lt $pkgBytes.Length; $i++) {
    $pkgBytes[$i] = [Convert]::ToByte($sha.Hash.Substring($i * 2, 2), 16)
}
$pkgChecksum = To-UrlSafeBase64 $pkgBytes

Write-Host "APK: $ApkPath"
Write-Host "PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM=$pkgChecksum"

$sdkRoot = $env:ANDROID_HOME
if (-not $sdkRoot) { $sdkRoot = $env:ANDROID_SDK_ROOT }
$apksigner = $null
if ($sdkRoot) {
    $found = Get-ChildItem -Path (Join-Path $sdkRoot "build-tools") -Recurse -Filter "apksigner.bat" -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending | Select-Object -First 1
    if ($found) { $apksigner = $found.FullName }
}

if ($apksigner) {
    $out = & $apksigner verify --print-certs $ApkPath 2>&1 | Out-String
    Write-Host $out
    if ($out -match "SHA-256 digest:\s*([0-9a-fA-F:]+)") {
        $hex = ($Matches[1] -replace ":", "")
        $sigBytes = [byte[]]::new($hex.Length / 2)
        for ($i = 0; $i -lt $sigBytes.Length; $i++) {
            $sigBytes[$i] = [Convert]::ToByte($hex.Substring($i * 2, 2), 16)
        }
        $sigChecksum = To-UrlSafeBase64 $sigBytes
        Write-Host "PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM=$sigChecksum" -ForegroundColor Green
    } else {
        Write-Host "WARN: could not parse SHA-256 from apksigner output" -ForegroundColor Yellow
    }
} else {
    Write-Host "WARN: apksigner not found (set ANDROID_HOME). PACKAGE_CHECKSUM only." -ForegroundColor Yellow
}
