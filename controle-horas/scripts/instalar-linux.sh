#!/bin/bash

# =============================================================
# INSTALADOR - CONTROLE DE HORAS
# =============================================================

set -e

APP_VERSION="1.0.1"

GITHUB_USER="ODiogorocha"
GITHUB_REPO="$GITHUB_USER/controle-de-horas"

INSTALL_DIR="$HOME/.local/share/controle-horas"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/.local/share/applications"

JAR_URL="https://github.com/$GITHUB_REPO/releases/latest/download/controle-horas.jar"
ICON_URL="https://github.com/$GITHUB_REPO/releases/latest/download/controle-horas.png"

JAR_LOCAL="$INSTALL_DIR/controle-horas.jar"
ICON_LOCAL="$INSTALL_DIR/controle-horas.png"

echo ""
echo "=================================================="
echo "      Sistema de Controle de Horas"
echo "=================================================="
echo ""

# =============================================================
# 1. JAVA
# =============================================================

echo "[1/6] Verificando Java..."

if ! command -v java &>/dev/null; then
    echo ""
    echo "ERRO: Java nao encontrado."
    echo ""
    echo "Instale Java 21:"
    echo "https://adoptium.net"
    echo ""
    exit 1
fi

echo "[OK] Java encontrado."

# =============================================================
# 2. PASTAS
# =============================================================

echo ""
echo "[2/6] Criando diretorios..."

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$DESKTOP_DIR"

echo "[OK] Diretorios criados."

# =============================================================
# 3. DOWNLOAD JAR
# =============================================================

echo ""
echo "[3/6] Baixando sistema..."

if command -v wget &>/dev/null; then
    wget -O "$JAR_LOCAL" "$JAR_URL"
else
    curl -L -o "$JAR_LOCAL" "$JAR_URL"
fi

if [ ! -f "$JAR_LOCAL" ]; then
    echo ""
    echo "ERRO: Falha ao baixar JAR."
    exit 1
fi

if [ ! -s "$JAR_LOCAL" ]; then
    echo ""
    echo "ERRO: JAR vazio."
    exit 1
fi

echo "[OK] Sistema baixado."

# =============================================================
# 4. DOWNLOAD ICONE
# =============================================================

echo ""
echo "[4/6] Baixando icone..."

if command -v wget &>/dev/null; then
    wget -O "$ICON_LOCAL" "$ICON_URL"
else
    curl -L -o "$ICON_LOCAL" "$ICON_URL"
fi

if [ -f "$ICON_LOCAL" ]; then
    echo "[OK] Icone baixado."
else
    echo "[AVISO] Icone nao encontrado."
fi

# =============================================================
# 5. SCRIPT DE EXECUCAO
# =============================================================

echo ""
echo "[5/6] Criando launcher..."

cat > "$BIN_DIR/controle-horas" << SCRIPT
#!/bin/bash
java -jar "$JAR_LOCAL"
SCRIPT

chmod +x "$BIN_DIR/controle-horas"

echo "[OK] Launcher criado."

# =============================================================
# 6. MENU
# =============================================================

echo ""
echo "[6/6] Criando menu..."

cat > "$DESKTOP_DIR/controle-horas.desktop" << DESKTOP
[Desktop Entry]
Version=1.0
Type=Application
Name=Controle de Horas
Comment=Sistema de Controle de Horas
Exec=$BIN_DIR/controle-horas
Icon=$ICON_LOCAL
Terminal=false
Categories=Office;
StartupNotify=true
DESKTOP

chmod +x "$DESKTOP_DIR/controle-horas.desktop"

echo "[OK] Atalho criado."

# =============================================================
# DESINSTALADOR
# =============================================================

cat > "$INSTALL_DIR/desinstalar.sh" << REMOVE
#!/bin/bash

echo "Desinstalando..."

rm -f "$BIN_DIR/controle-horas"
rm -f "$DESKTOP_DIR/controle-horas.desktop"

rm -rf "$INSTALL_DIR"

echo "Desinstalado."
REMOVE

chmod +x "$INSTALL_DIR/desinstalar.sh"

# =============================================================
# FINAL
# =============================================================

echo ""
echo "=================================================="
echo "         Instalacao concluida!"
echo "=================================================="
echo ""

echo "Abrir:"
echo "  Menu de aplicativos -> Controle de Horas"

echo ""
echo "Ou execute:"
echo "  controle-horas"

echo ""
read -p "Abrir agora? [s/N]: " abrir

if [[ "$abrir" =~ ^[Ss]$ ]]; then
    nohup java -jar "$JAR_LOCAL" &>/dev/null &
fi

echo ""