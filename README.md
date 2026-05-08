# Sistema de Controle de Horas

Sistema desktop para controle de ponto de funcionarios, calculo de horas trabalhadas e horas extras. Desenvolvido com Java 21 e Spring Boot.

---

## Instalacao Rapida

### Linux (Ubuntu, Debian, Fedora, Arch)

Abra o terminal e cole este unico comando:

```bash
curl -sSL https://github.com/SEU_USUARIO/controle-horas/releases/latest/download/instalar-linux.sh | bash
```

O instalador vai:
- Verificar e instalar o Java automaticamente se necessario
- Baixar o sistema
- Criar um atalho no menu de aplicativos
- Perguntar se voce quer abrir agora

### Windows 10 / 11

1. Va na pagina de [Releases][([https://github.com/SEU_USUARIO/controle-horas/releases/latest](https://github.com/ODiogorocha/controle-de-horas/blob/main/controle-horas/scripts/instalar-windows.bat](https://github.com/ODiogorocha/controle-de-horas/tree/main/controle-horas/scripts))
2. Baixe o arquivo **instalar-windows.bat**
3. Clique com o botao direito no arquivo > **Executar como administrador**
4. Siga as instrucoes na tela

O instalador vai:
- Instalar o Java automaticamente se necessario
- Baixar o sistema
- Criar um atalho na area de trabalho e no menu Iniciar
- Aparecer em Configuracoes > Aplicativos para desinstalar facilmente

---

## Como usar o sistema

O sistema segue 4 passos simples:

```
1. Cadastre os Cargos      (ex: Desenvolvedor - 8h/dia)
         |
         v
2. Cadastre os Funcionarios (nome, matricula, cargo)
         |
         v
3. Importe o arquivo CSV    (registros de entrada e saida)
         |
         v
4. Consulte o Relatorio     (horas trabalhadas, extras, faltando)
```

---

## Formato do arquivo CSV

Crie uma planilha no Excel ou LibreOffice com estas colunas e salve como CSV:

```
matricula,data,entrada,saida
EMP001,2025-01-13,08:00,17:30
EMP001,2025-01-14,08:15,18:00
EMP002,2025-01-13,09:00,18:30
```

| Coluna    | Formato       | Exemplo      |
|-----------|---------------|--------------|
| matricula | Texto         | EMP001       |
| data      | AAAA-MM-DD    | 2025-01-13   |
| entrada   | HH:MM         | 08:00        |
| saida     | HH:MM         | 17:30        |

---

## Requisitos

| Item      | Minimo               | Recomendado          |
|-----------|----------------------|----------------------|
| Java      | 17                   | 21 (instalado auto)  |
| RAM       | 256 MB               | 512 MB               |
| Disco     | 100 MB               | 200 MB               |
| SO        | Win 10 / Ubuntu 20   | Win 11 / Ubuntu 22   |

---

## Tecnologias usadas

- **Java 21** - linguagem de programacao
- **Spring Boot 3** - framework web e API REST
- **JavaFX** - interface grafica desktop
- **H2 Database** - banco de dados em memoria (sem instalacao)
- **OpenCSV** - leitura de arquivos CSV
- **Gradle** - gerenciamento de dependencias e build

---

## Para desenvolvedores

### Clonar e rodar localmente

```bash
git clone https://github.com/SEU_USUARIO/controle-horas.git
cd controle-horas
./gradlew bootRun
```

### Gerar o JAR

```bash
./gradlew bootJar
# O arquivo fica em: build/libs/controle-horas.jar
```

### Criar uma nova versao (release)

```bash
git tag v1.0.1
git push origin v1.0.1
```

O GitHub Actions vai automaticamente:
1. Compilar o projeto
2. Gerar o JAR
3. Criar uma Release com todos os arquivos de instalacao

---

## Estrutura do projeto

```
controle-horas/
├── .github/
│   └── workflows/
│       └── release.yml          <- Build e release automatico
├── scripts/
│   ├── instalar-linux.sh        <- Instalador Linux
│   ├── desinstalar-linux.sh     <- Desinstalador Linux
│   └── instalar-windows.bat     <- Instalador Windows
├── src/
│   └── main/
│       ├── java/com/empresa/controle_horas/
│       │   ├── model/           <- Entidades (Cargo, Funcionario, RegistroPonto)
│       │   ├── repository/      <- Acesso ao banco de dados
│       │   ├── service/         <- Calculo de horas e importacao CSV
│       │   ├── controller/      <- API REST
│       │   └── ui/              <- Interface grafica JavaFX
│       └── resources/
│           └── application.properties
├── build.gradle
└── README.md
```

---

## API REST

O sistema tambem expoe uma API REST em `http://localhost:8080/api`:

| Metodo | URL                                      | Descricao                |
|--------|------------------------------------------|--------------------------|
| GET    | /api/cargos                              | Lista cargos             |
| POST   | /api/cargos                              | Cria cargo               |
| GET    | /api/funcionarios                        | Lista funcionarios       |
| POST   | /api/funcionarios                        | Cadastra funcionario     |
| POST   | /api/ponto/importar                      | Importa CSV              |
| GET    | /api/ponto/relatorio?funcionarioId=1&... | Relatorio individual     |
| GET    | /api/ponto/relatorio/geral?...           | Relatorio de todos       |

Console do banco de dados: `http://localhost:8080/h2-console`

---

## Licenca

MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## Contribuindo

1. Fork o repositorio
2. Crie uma branch: `git checkout -b minha-melhoria`
3. Commit suas mudancas: `git commit -m 'Adiciona funcionalidade X'`
4. Push: `git push origin minha-melhoria`
5. Abra um Pull Request
