@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

title Instalador - Sistema de Controle de Horas

set "APP_VERSION=1.0.0"
set "INSTALL_DIR=%LOCALAPPDATA%\ControleHoras"
set "GITHUB_REPO=SEU_USUARIO/controle-horas"
set "JAR_URL=https://github.com/%GITHUB_REPO%/releases/latest/download/controle-horas.jar"

echo.
echo =============================================
echo    Sistema de Controle de Horas v%APP_VERSION%
echo    Instalador para Windows 10 e 11
echo =============================================
echo.

:: ── 1. Procura o Java instalado no sistema ───────────────────
echo [1/6] Verificando Java instalado...

set "JAVA_EXE="

:: Tenta encontrar pelo PATH
where javaw >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=*" %%i in ('where javaw') do (
        if not defined JAVA_EXE set "JAVA_EXE=%%i"
    )
)

:: Procura nas pastas padrao de instalacao do Java
if not defined JAVA_EXE (
    for /d %%i in (
        "%ProgramFiles%\Eclipse Adoptium\jre-21*"
        "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
        "%ProgramFiles%\Eclipse Adoptium\jre-17*"
        "%ProgramFiles%\Eclipse Adoptium\jdk-17*"
        "%ProgramFiles%\Java\jre*"
        "%ProgramFiles%\Java\jdk*"
        "%ProgramFiles%\Microsoft\jdk-21*"
        "%ProgramFiles%\Microsoft\jdk-17*"
        "%ProgramFiles%\Amazon Corretto\jre21*"
        "%ProgramFiles%\Amazon Corretto\jdk21*"
        "%ProgramFiles(x86)%\Java\jre*"
    ) do (
        if exist "%%i\bin\javaw.exe" (
            if not defined JAVA_EXE set "JAVA_EXE=%%i\bin\javaw.exe"
        )
    )
)

:: Procura pelo registro do Windows
if not defined JAVA_EXE (
    for %%k in (
        "HKLM\SOFTWARE\Eclipse Adoptium\JRE\21"
        "HKLM\SOFTWARE\Eclipse Adoptium\JDK\21"
        "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"
        "HKLM\SOFTWARE\JavaSoft\JDK"
    ) do (
        for /f "tokens=2*" %%a in (
            'reg query "%%~k" /v JavaHome 2^>nul ^| findstr JavaHome'
        ) do (
            if exist "%%b\bin\javaw.exe" (
                if not defined JAVA_EXE set "JAVA_EXE=%%b\bin\javaw.exe"
            )
        )
    )
)

if defined JAVA_EXE (
    echo [OK]   Java encontrado em: !JAVA_EXE!
    goto :java_encontrado
)

:: Java nao encontrado - instala automaticamente
echo [AVISO] Java nao encontrado. Instalando automaticamente...
echo         Aguarde, isso pode levar alguns minutos...
echo.

set "JAVA_INSTALLER=%TEMP%\java21-installer.msi"
set "JAVA_DL=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5+11/OpenJDK21U-jre_x64_windows_hotspot_21.0.5_11.msi"

echo [INFO] Baixando Java 21...
powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%JAVA_DL%', '%JAVA_INSTALLER%')"

if not exist "%JAVA_INSTALLER%" (
    echo.
    echo [ERRO] Nao foi possivel baixar o Java automaticamente.
    echo.
    echo        Instale manualmente:
    echo        1. Acesse: https://adoptium.net
    echo        2. Clique em "Latest LTS Release"
    echo        3. Escolha Windows x64 Installer
    echo        4. Execute o instalador baixado
    echo        5. Depois execute este instalador novamente
    echo.
    pause
    exit /b 1
)

echo [INFO] Instalando Java 21...
msiexec /i "%JAVA_INSTALLER%" /quiet /norestart
timeout /t 5 /nobreak >nul
del "%JAVA_INSTALLER%" >nul 2>&1

:: Procura o Java recem instalado
for /d %%i in (
    "%ProgramFiles%\Eclipse Adoptium\jre-21*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
) do (
    if exist "%%i\bin\javaw.exe" (
        if not defined JAVA_EXE set "JAVA_EXE=%%i\bin\javaw.exe"
    )
)

if not defined JAVA_EXE (
    echo [ERRO] Instalacao do Java falhou.
    echo        Instale manualmente em https://adoptium.net e tente novamente.
    pause
    exit /b 1
)

echo [OK]   Java instalado: !JAVA_EXE!

:java_encontrado

:: ── 2. Cria pasta de instalacao ──────────────────────────────
echo [2/6] Criando pasta de instalacao...
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
echo [OK]   %INSTALL_DIR%

:: ── 3. Baixa o JAR ───────────────────────────────────────────
echo [3/6] Baixando o sistema...
set "JAR_PATH=%INSTALL_DIR%\controle-horas.jar"

if exist "%JAR_PATH%" del "%JAR_PATH%"

powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%JAR_URL%', '%JAR_PATH%')"

if not exist "%JAR_PATH%" (
    echo [ERRO] Falha ao baixar. Verifique sua internet e tente novamente.
    pause
    exit /b 1
)
echo [OK]   Sistema baixado.

:: ── 4. Cria o launcher ───────────────────────────────────────
echo [4/6] Criando iniciador...
set "LAUNCHER=%INSTALL_DIR%\iniciar.bat"

(
    echo @echo off
    echo cd /d "%INSTALL_DIR%"
    echo start "" "!JAVA_EXE!" -jar "%JAR_PATH%"
) > "%LAUNCHER%"

:: Salva o caminho do Java para uso futuro
echo !JAVA_EXE!> "%INSTALL_DIR%\java_path.txt"

echo [OK]   Iniciador criado.

:: ── 5. Cria atalhos ──────────────────────────────────────────
echo [5/6] Criando atalhos...

set "DESKTOP=%USERPROFILE%\Desktop"
set "STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs"

:: Cria atalho na area de trabalho
powershell -NoProfile -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%DESKTOP%\Controle de Horas.lnk'); $s.TargetPath = '!JAVA_EXE!'; $s.Arguments = '-jar \"%JAR_PATH%\"'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Description = 'Sistema de Controle de Horas e Ponto'; $s.WindowStyle = 1; $s.Save()"

:: Cria atalho no menu Iniciar
powershell -NoProfile -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%STARTMENU%\Controle de Horas.lnk'); $s.TargetPath = '!JAVA_EXE!'; $s.Arguments = '-jar \"%JAR_PATH%\"'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Description = 'Sistema de Controle de Horas e Ponto'; $s.WindowStyle = 1; $s.Save()"

if exist "%DESKTOP%\Controle de Horas.lnk" (
    echo [OK]   Atalho criado na area de trabalho.
) else (
    echo [AVISO] Atalho nao criado. Use o arquivo: %LAUNCHER%
)

if exist "%STARTMENU%\Controle de Horas.lnk" (
    echo [OK]   Atalho criado no menu Iniciar.
)

:: ── 6. Registra desinstalador ────────────────────────────────
echo [6/6] Registrando desinstalador...

set "UNINSTALL_BAT=%INSTALL_DIR%\desinstalar.bat"
(
    echo @echo off
    echo chcp 65001 ^>nul 2^>^&1
    echo echo Desinstalando Sistema de Controle de Horas...
    echo set /p "ok=Tem certeza? [s/N]: "
    echo if /i not "%%ok%%"=="s" exit /b 0
    echo del "%DESKTOP%\Controle de Horas.lnk" 2^>nul
    echo del "%STARTMENU%\Controle de Horas.lnk" 2^>nul
    echo reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\ControleHoras" /f 2^>nul
    echo rmdir /s /q "%INSTALL_DIR%"
    echo echo Desinstalado com sucesso!
    echo pause
) > "%UNINSTALL_BAT%"

set "REG=HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\ControleHoras"
reg add "%REG%" /v "DisplayName"     /t REG_SZ    /d "Sistema de Controle de Horas" /f >nul
reg add "%REG%" /v "DisplayVersion"  /t REG_SZ    /d "%APP_VERSION%"                /f >nul
reg add "%REG%" /v "Publisher"       /t REG_SZ    /d "github.com/%GITHUB_REPO%"     /f >nul
reg add "%REG%" /v "InstallLocation" /t REG_SZ    /d "%INSTALL_DIR%"                /f >nul
reg add "%REG%" /v "UninstallString" /t REG_SZ    /d "%UNINSTALL_BAT%"              /f >nul
reg add "%REG%" /v "NoModify"        /t REG_DWORD /d 1                              /f >nul

echo [OK]   Registrado em Configuracoes > Aplicativos.

:: ── Conclusao ─────────────────────────────────────────────────
echo.
echo =============================================
echo    Instalacao concluida com sucesso!
echo =============================================
echo.
echo   Java: !JAVA_EXE!
echo.
echo   Como abrir:
echo   - Icone "Controle de Horas" na area de trabalho
echo   - Menu Iniciar ^> Controle de Horas
echo   - Direto: %LAUNCHER%
echo.
echo   Para desinstalar:
echo   Configuracoes ^> Aplicativos ^> Controle de Horas
echo.

set /p "abrir=Deseja abrir o sistema agora? [s/N]: "
if /i "%abrir%"=="s" (
    start "" "!JAVA_EXE!" -jar "%JAR_PATH%"
    echo [OK] Abrindo...
)

echo.
pause