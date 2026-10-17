param(
    [ValidateSet('Build', 'Run', 'Test')][string]$Action = 'Build',
    [string]$Serial = '',
    [ValidateRange(1, 65535)][int]$AdbPort = 5037,
    [string]$SdkPath = $env:ANDROID_HOME
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $SdkPath) { $SdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path -LiteralPath $SdkPath)) { throw 'Set ANDROID_HOME or pass -SdkPath with your Android SDK directory.' }
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr' }
if (-not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { throw 'Set JAVA_HOME to a compatible JDK.' }
$env:ANDROID_HOME = $SdkPath
if (-not $env:ANDROID_USER_HOME) { $env:ANDROID_USER_HOME = Join-Path $env:USERPROFILE '.android' }
if (-not $env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle' }
$gradleTemp = Join-Path $projectRoot '.gradle\tmp'
New-Item -ItemType Directory -Path $gradleTemp -Force | Out-Null
# A short socket directory avoids Windows/JBR Unix-domain path-length failures.
$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Djava.net.preferIPv4Stack=true -Djdk.net.unixdomain.tmpdir=$gradleTemp -Djava.io.tmpdir=$gradleTemp".Trim()
$adb = Join-Path $SdkPath 'platform-tools\adb.exe'
$adbOptions = @('-P', "$AdbPort")

Push-Location $projectRoot
try {
    $tasks = @(':androidApp:assembleDebug', ':composeApp:testAndroidHostTest')
    if ($Action -eq 'Test') { $tasks += ':androidApp:assembleDebugAndroidTest' }
    & .\gradlew.bat @tasks --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed.' }
    if ($Action -eq 'Build') { return }

    if (-not $Serial) {
        $emulators = @(& $adb @adbOptions devices | Select-String '^emulator-\d+\s+device$' | ForEach-Object { ($_.Line -split '\s+')[0] })
        if ($emulators.Count -ne 1) { throw 'Start one emulator, or pass -Serial emulator-PORT explicitly.' }
        $Serial = $emulators[0]
    }
    if ($Serial -notmatch '^emulator-\d+$') { throw 'This prototype helper intentionally targets emulators only.' }
    & $adb @adbOptions -s $Serial install -r 'androidApp\build\outputs\apk\debug\androidApp-debug.apk'
    if ($LASTEXITCODE -ne 0) { throw 'App install failed.' }
    if ($Action -eq 'Run') {
        & $adb @adbOptions -s $Serial shell am start -n com.shadcn.mobile/.MainActivity
        if ($LASTEXITCODE -ne 0) { throw 'App launch failed.' }
        return
    }

    & $adb @adbOptions -s $Serial install -r 'androidApp\build\outputs\apk\androidTest\debug\androidApp-debug-androidTest.apk'
    if ($LASTEXITCODE -ne 0) { throw 'Test APK install failed.' }
    New-Item -ItemType Directory -Path docs -Force | Out-Null
    $result = & $adb @adbOptions -s $Serial shell am instrument -w -r com.shadcn.mobile.test/androidx.test.runner.AndroidJUnitRunner
    $runnerExit = $LASTEXITCODE
    $result | Tee-Object -FilePath 'docs\instrumentation-run.txt'
    if ($runnerExit -ne 0 -or ($result -join "`n") -notmatch 'OK \(\d+ tests?\)') { throw 'Instrumentation did not finish successfully. See docs/instrumentation-run.txt.' }
    New-Item -ItemType Directory -Path 'docs\screenshots' -Force | Out-Null
    & $adb @adbOptions -s $Serial pull '/sdcard/Android/data/com.shadcn.mobile/files/screenshots/.' 'docs\screenshots'
    if ($LASTEXITCODE -ne 0) { throw 'Tests passed, but screenshot retrieval failed.' }
} finally {
    Pop-Location
}
