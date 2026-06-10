param(
    [ValidateSet("app-image", "msi")]
    [string]$Type = "app-image"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$targetDirectory = Join-Path $projectRoot "target"
$inputDirectory = Join-Path $targetDirectory "package-input"
$outputDirectory = Join-Path $targetDirectory "dist"
$mainJar = "logscope-desktop-0.1.0-SNAPSHOT.jar"
$mainClass = "nl.itqaanconsulting.logscope.LogScopeLauncher"

function Find-JPackage {
    $candidates = [System.Collections.Generic.List[string]]::new()

    if ($env:JAVA_HOME) {
        $candidates.Add((Join-Path $env:JAVA_HOME "bin\jpackage.exe"))
    }

    $pathCommand = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($pathCommand) {
        $candidates.Add($pathCommand.Source)
    }

    $javaRoot = Join-Path $env:ProgramFiles "Java"
    if (Test-Path -LiteralPath $javaRoot) {
        Get-ChildItem -LiteralPath $javaRoot -Directory |
                Sort-Object Name -Descending |
                ForEach-Object { $candidates.Add((Join-Path $_.FullName "bin\jpackage.exe")) }
    }

    $jpackage = $candidates |
            Where-Object { Test-Path -LiteralPath $_ } |
            Select-Object -First 1

    if (-not $jpackage) {
        throw "jpackage.exe was not found. Install a full JDK 21 or newer and set JAVA_HOME."
    }

    return $jpackage
}

$jpackage = Find-JPackage
$resolvedTarget = [System.IO.Path]::GetFullPath($targetDirectory)
$resolvedOutput = [System.IO.Path]::GetFullPath($outputDirectory)
if (-not $resolvedOutput.StartsWith($resolvedTarget + [System.IO.Path]::DirectorySeparatorChar)) {
    throw "Refusing to clean output outside the Maven target directory: $resolvedOutput"
}

Push-Location $projectRoot
try {
    & mvn.cmd clean package dependency:copy-dependencies `
            "-DincludeScope=runtime" `
            "-DoutputDirectory=$inputDirectory"
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }

    Copy-Item -LiteralPath (Join-Path $targetDirectory $mainJar) `
            -Destination (Join-Path $inputDirectory $mainJar) `
            -Force

    if (Test-Path -LiteralPath $outputDirectory) {
        Remove-Item -LiteralPath $outputDirectory -Recurse -Force
    }

    $arguments = @(
        "--type", $Type,
        "--name", "LogScope",
        "--app-version", "0.1.0",
        "--vendor", "Itqaan Consulting",
        "--description", "Desktop log analysis application",
        "--input", $inputDirectory,
        "--dest", $outputDirectory,
        "--main-jar", $mainJar,
        "--main-class", $mainClass
    )

    if ($Type -eq "msi") {
        $arguments += @(
            "--win-menu",
            "--win-shortcut",
            "--win-dir-chooser"
        )
    }

    & $jpackage @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE."
    }

    if ($Type -eq "app-image") {
        Write-Host "Created $outputDirectory\LogScope\LogScope.exe"
    } else {
        Write-Host "Created Windows installer in $outputDirectory"
    }
}
finally {
    Pop-Location
}
