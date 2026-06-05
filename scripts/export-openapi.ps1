# 从 Smart-EMAPs 后端导出 OpenAPI，供 Android 代码生成使用
param(
    [string]$ApiBase = "http://localhost:8005",
    [string]$OutFile = "$PSScriptRoot\..\contracts\openapi.json"
)

$dir = Split-Path $OutFile -Parent
if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

$url = "$ApiBase/openapi.json"
Write-Host "Fetching $url ..."
try {
    Invoke-WebRequest -Uri $url -OutFile $OutFile -UseBasicParsing
    Write-Host "Saved: $OutFile"
} catch {
    Write-Error "导出失败。请先启动 Smart-EMAPs 后端: $ApiBase"
    exit 1
}
