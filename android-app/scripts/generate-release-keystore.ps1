param(
    [string]$KeystorePath = "keystore/day_line-release.jks",
    [string]$Alias = "day_line",
    [int]$ValidityDays = 9125,
    [string]$DName = "CN=TOUTAKUN04, OU=Mobile, O=Day Line, L=Unknown, ST=Unknown, C=US"
)

$ErrorActionPreference = "Stop"

function ConvertTo-PlainText {
    param([System.Security.SecureString]$SecureValue)
    if ($null -eq $SecureValue) { return "" }
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

$keytool = Get-Command keytool -ErrorAction SilentlyContinue
if ($null -eq $keytool) {
    throw "keytool was not found. Install JDK 17+ and ensure keytool is on PATH."
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$absoluteKeystorePath = Join-Path $repoRoot $KeystorePath
$keystoreDirectory = Split-Path -Parent $absoluteKeystorePath

if (!(Test-Path $keystoreDirectory)) {
    New-Item -ItemType Directory -Path $keystoreDirectory -Force | Out-Null
}

if (Test-Path $absoluteKeystorePath) {
    throw "Keystore already exists at '$absoluteKeystorePath'. Use a different path or remove it first."
}

$storePassword = ConvertTo-PlainText (Read-Host "Enter keystore password" -AsSecureString)
if ([string]::IsNullOrWhiteSpace($storePassword)) {
    throw "Keystore password cannot be empty."
}

$keyPasswordInput = ConvertTo-PlainText (Read-Host "Enter key password (press Enter to reuse keystore password)" -AsSecureString)
$keyPassword = if ([string]::IsNullOrWhiteSpace($keyPasswordInput)) { $storePassword } else { $keyPasswordInput }

& $keytool.Path `
    -genkeypair `
    -v `
    -keystore $absoluteKeystorePath `
    -alias $Alias `
    -keyalg RSA `
    -keysize 4096 `
    -validity $ValidityDays `
    -storepass $storePassword `
    -keypass $keyPassword `
    -dname $DName

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed with exit code $LASTEXITCODE"
}

$propertiesPath = Join-Path $repoRoot "keystore.properties"
$normalizedKeystorePath = $KeystorePath.Replace('\', '/')

@(
    "storeFile=$normalizedKeystorePath"
    "storePassword=$storePassword"
    "keyAlias=$Alias"
    "keyPassword=$keyPassword"
) | Set-Content -Path $propertiesPath -Encoding Ascii

Write-Host "Release keystore created at: $absoluteKeystorePath"
Write-Host "Signing config written to: $propertiesPath"
Write-Host "Do not commit keystore.properties or the keystore file."
