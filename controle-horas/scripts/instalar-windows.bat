@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

title Instalador - Sistema de Controle de Horas

:: ============================================================
:: CONFIGURACOES
:: ============================================================

set "APP_VERSION=1.0.1"

set "GITHUB_USER=ODiogorocha"
set "GITHUB_REPO=%GITHUB_USER%/controle-de-horas"

set "INSTALL_DIR=%LOCALAPPDATA%\ControleHoras"

set "JAR_URL=https://github.com/%GITHUB_REPO%/releases/latest/download/controle-horas.jar"
set "ICON_URL=https://github.com/%GITHUB_REPO%/releases/latest/download/controle-horas.ico"

set "JAR_PATH=%INSTALL_DIR%\controle-horas.jar"
set "ICON_PATH=%INSTALL_DIR%\controle-horas.ico"

echo.
echo ==================================================
echo       Sistema de Controle de Horas
echo ==================================================
echo.

:: ============================================================
:: 1. VERIFICA JAVA
:: ============================================================

echo [1/6] Verificando Java...

set "JAVA_EXE="

where javaw >nul 2>&1

if %ERRORLEVEL% EQU 0 (
    for /f "tokens=*" %%i in ('where javaw') do (
        if not defined JAVA_EXE (
            set "JAVA_EXE=%%i"
        )
    )
)

if not defined JAVA_EXE (
    echo.
    echo [ERRO] Java nao encontrado.
    echo.
    echo Instale Java 21:
    echo https://adoptium.net
    echo.
    pause
    exit /b 1
)

echo [OK] Java encontrado:
echo      !JAVA_EXE!

:: ============================================================
:: 2. CRIA PASTA
:: ============================================================

echo.
echo [2/6] Criando pasta de instalacao...

if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
)

echo [OK] Pasta criada.

:: ============================================================
:: 3. BAIXA JAR
:: ============================================================

echo.
echo [3/6] Baixando sistema...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
 "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
  $wc = New-Object System.Net.WebClient; ^
  $wc.DownloadFile('%JAR_URL%', '%JAR_PATH%')"

if not exist "%JAR_PATH%" (
    echo.
    echo [ERRO] Falha ao baixar o sistema.
    pause
    exit /b 1
)

echo [OK] Sistema baixado.

:: ============================================================
:: 4. BAIXA ICONE
:: ============================================================

echo.
echo [4/6] Baixando icone...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
 "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
  $wc = New-Object System.Net.WebClient; ^
  $wc.DownloadFile('%ICON_URL%', '%ICON_PATH%')"

if exist "%ICON_PATH%" (
    echo [OK] Icone baixado.
) else (
    echo [AVISO] Icone nao encontrado.
)

:: ============================================================
:: 5. CRIA LAUNCHER
:: ============================================================

echo.
echo [5/6] Criando launcher...

set "VBS=%INSTALL_DIR%\iniciar.vbs"

(
echo Set WshShell = CreateObject("WScript.Shell"^)
echo WshShell.Run chr(34^) ^& "!JAVA_EXE!" ^& chr(34^) ^& " -jar " ^& chr(34^) ^& "%JAR_PATH%" ^& chr(34^), 0
echo Set WshShell = Nothing
) > "%VBS%"

echo [OK] Launcher criado.

:: ============================================================
:: 6. CRIA ATALHOS
:: ============================================================

echo.
echo [6/6] Criando atalhos...

set "DESKTOP=%USERPROFILE%\Desktop"
set "STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
 "$ws = New-Object -ComObject WScript.Shell; ^
  $s = $ws.CreateShortcut('%DESKTOP%\Controle de Horas.lnk'); ^
  $s.TargetPath = '%VBS%'; ^
  $s.WorkingDirectory = '%INSTALL_DIR%'; ^
  $s.Description = 'Sistema de Controle de Horas'; ^
  $s.IconLocation = '%ICON_PATH%'; ^
  $s.Save()"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
 "$ws = New-Object -ComObject WScript.Shell; ^
  $s = $ws.CreateShortcut('%STARTMENU%\Controle de Horas.lnk'); ^
  $s.TargetPath = '%VBS%'; ^
  $s.WorkingDirectory = '%INSTALL_DIR%'; ^
  $s.Description = 'Sistema de Controle de Horas'; ^
  $s.IconLocation = '%ICON_PATH%'; ^
  $s.Save()"

echo [OK] Atalhos criados.

:: ============================================================
:: FINAL
:: ============================================================

echo.
echo ==================================================
echo           Instalacao concluida!
echo ==================================================
echo.

echo Area de trabalho:
echo   Controle de Horas

echo.
echo Menu iniciar:
echo   Controle de Horas

echo.
set /p "abrir=Abrir agora? [s/N]: "

if /i "%abrir%"=="s" (
    start "" "%VBS%"
)

echo.
pause