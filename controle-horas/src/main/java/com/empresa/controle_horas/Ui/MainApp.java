package com.empresa.controle_horas.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainApp extends Application {

    private static final String API = "http://localhost:8080/api";
    private final HttpClient http = HttpClient.newHttpClient();

    // Campos Cargos
    private TextField cargoNomeField;
    private TextField horasField;

    // Campos Funcionarios
    private TextField nomeField;
    private TextField matriculaField;
    private TextField cargoIdField;

    // Campos Relatorio
    private TextField relFuncIdField;
    private TextField relInicioField;
    private TextField relFimField;

    // Campos Escala Automatica
    private TextField escalaInicioField;
    private TextField escalaFimField;
    private TextField escalaAberturaField;
    private TextField escalaFechamentoField;
    private TextField escalaHorasTurnoField;
    private TextField escalaSetorField;

    // Componentes globais
    private VBox resultadoBox;
    private TextArea logArea;
    private Label statusBar;
    private Circle statusDot;

    // ── Paleta de cores ───────────────────────────────────────
    private static final String AZUL_ESCURO   = "#0D2137";
    private static final String AZUL_MEDIO    = "#1A3C5E";
    private static final String AZUL_ACENTO   = "#2E86C1";
    private static final String AZUL_CLARO    = "#5DADE2";
    private static final String VERDE         = "#1E8449";
    private static final String VERDE_CLARO   = "#27AE60";
    private static final String VERMELHO      = "#C0392B";
    private static final String LARANJA       = "#D35400";
    private static final String FUNDO         = "#EEF2F7";
    private static final String FUNDO_CARD    = "#FFFFFF";
    private static final String BORDA         = "#D5DCE8";
    private static final String TEXTO         = "#1C2833";
    private static final String TEXTO_FRACO   = "#6C7A89";
    private static final String DICA_FUNDO    = "#EBF5FB";
    private static final String DICA_BORDA    = "#AED6F1";
    private static final String SUCESSO_FUNDO = "#EAFAF1";
    private static final String SUCESSO_BORDA = "#A9DFBF";
    private static final String SIDEBAR       = "#1A2B3C";
    private static final String SIDEBAR_HOVER = "#243447";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Controle de Horas — Sistema de Gestao de Ponto");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FUNDO + ";");

        root.setTop(criarCabecalho());
        root.setLeft(criarSidebar());
        root.setCenter(criarAreaCentral());
        root.setBottom(criarBarraInferior());

        Scene scene = new Scene(root, 1100, 720);
        stage.setScene(scene);
        stage.show();

        setStatus("Sistema iniciado. Servidor rodando em localhost:8080.", true);
        log("Bem-vindo ao Sistema de Controle de Horas!");
        log("Navegue pelas abas para gerenciar cargos, funcionarios, ponto e escalas.");
    }

    // ── Cabecalho ─────────────────────────────────────────────
    private HBox criarCabecalho() {
        HBox header = new HBox();
        header.setPrefHeight(64);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, " + AZUL_ESCURO + ", " + AZUL_MEDIO + ");"
        );

        // Bloco esquerdo: titulo
        VBox blocoTitulo = new VBox(2);
        blocoTitulo.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Controle de Horas");
        titulo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitulo = new Label("Sistema de Gestao de Ponto e Horas Extras");
        subtitulo.setStyle(
                "-fx-text-fill: #85C1E9;" +
                        "-fx-font-size: 11px;"
        );

        blocoTitulo.getChildren().addAll(titulo, subtitulo);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        // Bloco direito: indicador de status
        HBox statusBloco = new HBox(8);
        statusBloco.setAlignment(Pos.CENTER);

        statusDot = new Circle(6);
        statusDot.setFill(Color.web("#2ECC71"));

        VBox statusTextos = new VBox(1);
        statusTextos.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("API Conectada");
        statusLabel.setStyle("-fx-text-fill: #A9CCE3; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label urlLabel = new Label("localhost:8080");
        urlLabel.setStyle("-fx-text-fill: #5D8AA8; -fx-font-size: 10px;");

        statusTextos.getChildren().addAll(statusLabel, urlLabel);
        statusBloco.getChildren().addAll(statusDot, statusTextos);

        // Versao
        Label versao = new Label("v1.0");
        versao.setStyle(
                "-fx-text-fill: #5D8AA8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-background-color: #0D2137;" +
                        "-fx-padding: 3 8;" +
                        "-fx-background-radius: 10;"
        );

        header.getChildren().addAll(blocoTitulo, espacador, statusBloco, new Label("  "), versao);
        return header;
    }

    // ── Sidebar com guia de passos ─────────────────────────────
    private VBox criarSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color: " + SIDEBAR + ";");

        // Titulo da sidebar
        Label labelGuia = new Label("GUIA DE USO");
        labelGuia.setStyle(
                "-fx-text-fill: #5D8AA8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 20 0 10 16;"
        );
        sidebar.getChildren().add(labelGuia);

        // Itens do guia
        String[][] passos = {
                {"1", "Cargos",          "Cadastre os cargos\ne carga horaria"},
                {"2", "Funcionarios",    "Cadastre sua\nequipe de trabalho"},
                {"3", "Importar Ponto",  "Carregue o CSV\ncom os registros"},
                {"4", "Relatorio",       "Consulte horas\ne banco de horas"},
                {"5", "Escala",          "Gere escalas\nautomaticamente"},
        };

        for (String[] passo : passos) {
            HBox item = new HBox(12);
            item.setPadding(new Insets(12, 12, 12, 16));
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(
                    "-fx-border-color: transparent transparent #243447 transparent;" +
                            "-fx-border-width: 0 0 1 0;" +
                            "-fx-cursor: hand;"
            );

            // Numero do passo
            StackPane numeroBadge = new StackPane();
            numeroBadge.setMinSize(28, 28);
            numeroBadge.setMaxSize(28, 28);
            numeroBadge.setStyle(
                    "-fx-background-color: " + AZUL_ACENTO + ";" +
                            "-fx-background-radius: 14;"
            );
            Label numero = new Label(passo[0]);
            numero.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
            numeroBadge.getChildren().add(numero);

            // Textos
            VBox textos = new VBox(2);
            Label lNome = new Label(passo[1]);
            lNome.setStyle("-fx-text-fill: #D6EAF8; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label lDesc = new Label(passo[2]);
            lDesc.setStyle("-fx-text-fill: #5D8AA8; -fx-font-size: 10px;");
            textos.getChildren().addAll(lNome, lDesc);

            item.getChildren().addAll(numeroBadge, textos);

            item.setOnMouseEntered(e ->
                    item.setStyle(item.getStyle() + "-fx-background-color: " + SIDEBAR_HOVER + ";"));
            item.setOnMouseExited(e ->
                    item.setStyle(
                            "-fx-border-color: transparent transparent #243447 transparent;" +
                                    "-fx-border-width: 0 0 1 0;" +
                                    "-fx-cursor: hand;"
                    ));

            sidebar.getChildren().add(item);
        }

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        sidebar.getChildren().add(espaco);

        // Rodape da sidebar
        VBox rodape = new VBox(3);
        rodape.setPadding(new Insets(12, 12, 16, 16));
        rodape.setStyle("-fx-background-color: #111D28;");
        for (String linha : new String[]{"Sistema v1.0", "Java 21 + Spring Boot", "Banco: H2 (memoria)"}) {
            Label l = new Label(linha);
            l.setStyle("-fx-text-fill: #3D566E; -fx-font-size: 10px;");
            rodape.getChildren().add(l);
        }
        sidebar.getChildren().add(rodape);

        return sidebar;
    }

    // ── Area central com abas ──────────────────────────────────
    private ScrollPane criarAreaCentral() {
        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.setStyle(
                "-fx-background-color: " + FUNDO + ";" +
                        "-fx-tab-min-width: 130px;" +
                        "-fx-tab-min-height: 38px;"
        );

        abas.getTabs().addAll(
                criarAbaCargos(),
                criarAbaFuncionarios(),
                criarAbaImportarCSV(),
                criarAbaRelatorio(),
                criarAbaEscala()
        );

        ScrollPane scroll = new ScrollPane(abas);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        return scroll;
    }

    // ── Aba 1: Cargos ─────────────────────────────────────────
    private Tab criarAbaCargos() {
        Tab tab = new Tab("  1. Cargos  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(criarBannerInfo(
                "O que e um Cargo?",
                "Define a funcao do funcionario e quantas horas ele deve trabalhar por dia.\n" +
                        "Cadastre os cargos antes de cadastrar funcionarios.",
                DICA_FUNDO, DICA_BORDA, AZUL_ACENTO
        ));

        VBox card = criarCard("Cadastrar Novo Cargo");

        VBox campoNome = criarCampoFormulario(
                "Nome do Cargo",
                "Ex: Desenvolvedor, Analista, Gerente, Estagiario",
                "Digite o nome da funcao do funcionario na empresa"
        );
        cargoNomeField = (TextField) campoNome.getChildren().get(1);

        VBox campoHoras = criarCampoFormulario(
                "Horas de Trabalho por Dia",
                "Ex: 8.0",
                "Use ponto para decimal: 8.0 = oito horas | 6.5 = seis horas e meia"
        );
        horasField = (TextField) campoHoras.getChildren().get(1);

        HBox botoes = new HBox(12);
        botoes.setPadding(new Insets(8, 0, 0, 0));
        Button btnSalvar = botaoPrimario("Salvar Cargo");
        btnSalvar.setOnAction(e -> criarCargo());
        Button btnListar = botaoSecundario("Ver Todos os Cargos");
        btnListar.setOnAction(e -> listarCargos());
        botoes.getChildren().addAll(btnSalvar, btnListar);

        card.getChildren().addAll(campoNome, separador(), campoHoras, botoes);
        pagina.getChildren().add(card);

        pagina.getChildren().add(criarBannerInfo(
                "Exemplo",
                "Cargo: Desenvolvedor  ->  Horas por dia: 8.0\n" +
                        "Cargo: Estagiario     ->  Horas por dia: 6.0\n" +
                        "Cargo: Gerente        ->  Horas por dia: 8.0",
                SUCESSO_FUNDO, SUCESSO_BORDA, VERDE
        ));

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 2: Funcionarios ───────────────────────────────────
    private Tab criarAbaFuncionarios() {
        Tab tab = new Tab("  2. Funcionarios  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(criarBannerInfo(
                "Antes de comecar",
                "Voce precisa ter pelo menos um Cargo cadastrado.\n" +
                        "A matricula sera usada no arquivo CSV para identificar o funcionario.",
                DICA_FUNDO, DICA_BORDA, AZUL_ACENTO
        ));

        VBox card = criarCard("Cadastrar Novo Funcionario");

        VBox campoNome = criarCampoFormulario(
                "Nome Completo",
                "Ex: Maria Oliveira Santos",
                "Nome completo como aparece nos documentos"
        );
        nomeField = (TextField) campoNome.getChildren().get(1);

        VBox campoMat = criarCampoFormulario(
                "Matricula",
                "Ex: EMP001",
                "Codigo unico do funcionario. Deve ser o mesmo usado no arquivo CSV."
        );
        matriculaField = (TextField) campoMat.getChildren().get(1);

        VBox campoCargo = criarCampoFormulario(
                "ID do Cargo",
                "Ex: 1",
                "Numero do cargo. Clique em 'Ver Todos os Cargos' na aba anterior para consultar."
        );
        cargoIdField = (TextField) campoCargo.getChildren().get(1);

        HBox botoes = new HBox(12);
        botoes.setPadding(new Insets(8, 0, 0, 0));
        Button btnSalvar = botaoPrimario("Salvar Funcionario");
        btnSalvar.setOnAction(e -> criarFuncionario());
        Button btnListar = botaoSecundario("Ver Todos os Funcionarios");
        btnListar.setOnAction(e -> listarFuncionarios());
        botoes.getChildren().addAll(btnSalvar, btnListar);

        card.getChildren().addAll(campoNome, separador(), campoMat, separador(), campoCargo, botoes);
        pagina.getChildren().add(card);

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 3: Importar CSV ───────────────────────────────────
    private Tab criarAbaImportarCSV() {
        Tab tab = new Tab("  3. Importar Ponto  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(criarBannerInfo(
                "O que e o arquivo CSV?",
                "Um arquivo de planilha simples criado no Excel ou LibreOffice.\n" +
                        "Ele registra quem trabalhou, em qual data, e os horarios de entrada e saida.",
                DICA_FUNDO, DICA_BORDA, AZUL_ACENTO
        ));

        // Card de formato
        VBox cardFormato = criarCard("Formato do Arquivo CSV");

        Label lFormato = new Label("O arquivo deve ter exatamente estas 4 colunas:");
        lFormato.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 12px;");

        Label lExemplo = new Label(
                "matricula,data,entrada,saida\n" +
                        "EMP001,2025-01-13,08:00,17:30\n" +
                        "EMP001,2025-01-14,08:15,18:00\n" +
                        "EMP002,2025-01-13,09:00,18:30"
        );
        lExemplo.setStyle(
                "-fx-background-color: #1C2833;" +
                        "-fx-text-fill: #58D68D;" +
                        "-fx-font-family: monospace;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 6;"
        );
        lExemplo.setMaxWidth(Double.MAX_VALUE);

        // Tabela de colunas
        GridPane tabela = new GridPane();
        tabela.setHgap(0); tabela.setVgap(0);
        tabela.setStyle("-fx-border-color: " + BORDA + "; -fx-border-width: 1; -fx-border-radius: 6;");

        String[] cols = {"matricula", "data", "entrada", "saida"};
        String[] descs = {"Codigo do funcionario\nEx: EMP001", "Formato: ANO-MES-DIA\nEx: 2025-01-15", "Formato: HH:MM\nEx: 08:00", "Formato: HH:MM\nEx: 17:30"};

        for (int i = 0; i < cols.length; i++) {
            VBox cabecalho = new VBox(2);
            cabecalho.setPadding(new Insets(8, 16, 8, 16));
            cabecalho.setStyle("-fx-background-color: " + AZUL_MEDIO + ";" +
                    (i < cols.length - 1 ? "-fx-border-color: transparent #2C5282 transparent transparent; -fx-border-width: 0 1 0 0;" : ""));
            Label lCol = new Label(cols[i]);
            lCol.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
            cabecalho.getChildren().add(lCol);
            tabela.add(cabecalho, i, 0);

            VBox celDesc = new VBox(2);
            celDesc.setPadding(new Insets(8, 16, 8, 16));
            celDesc.setStyle("-fx-background-color: white;" +
                    (i < cols.length - 1 ? "-fx-border-color: transparent " + BORDA + " transparent transparent; -fx-border-width: 0 1 0 0;" : ""));
            Label lDesc = new Label(descs[i]);
            lDesc.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");
            lDesc.setWrapText(true);
            celDesc.getChildren().add(lDesc);
            tabela.add(celDesc, i, 1);
        }

        cardFormato.getChildren().addAll(lFormato, tabela, lExemplo);
        pagina.getChildren().add(cardFormato);

        // Card de upload
        VBox cardUpload = criarCard("Selecionar e Importar Arquivo");

        Label lDesc = new Label(
                "Selecione o arquivo CSV do seu computador.\n" +
                        "Certifique-se de que as matriculas ja estao cadastradas no sistema."
        );
        lDesc.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 12px;");
        lDesc.setWrapText(true);

        final File[] arquivoSelecionado = {null};

        Label arquivoLabel = new Label("Nenhum arquivo selecionado ainda.");
        arquivoLabel.setMaxWidth(Double.MAX_VALUE);
        arquivoLabel.setStyle(
                "-fx-text-fill: " + TEXTO_FRACO + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 8 12;" +
                        "-fx-background-color: " + FUNDO + ";" +
                        "-fx-border-color: " + BORDA + ";" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );

        Button btnEscolher = botaoSecundario("Escolher arquivo CSV...");
        btnEscolher.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Selecionar arquivo de ponto");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
            File f = fc.showOpenDialog(btnEscolher.getScene().getWindow());
            if (f != null) {
                arquivoSelecionado[0] = f;
                arquivoLabel.setText("Selecionado: " + f.getName() + "  (" + f.length() / 1024 + " KB)");
                arquivoLabel.setStyle(
                        "-fx-text-fill: " + VERDE + ";" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 8 12;" +
                                "-fx-background-color: " + SUCESSO_FUNDO + ";" +
                                "-fx-border-color: " + SUCESSO_BORDA + ";" +
                                "-fx-border-radius: 4;" +
                                "-fx-background-radius: 4;"
                );
            }
        });

        Button btnImportar = botaoCor("Importar Registros de Ponto", VERDE, "#145A32");
        btnImportar.setOnAction(e -> {
            if (arquivoSelecionado[0] == null) {
                log("ATENCAO: Selecione um arquivo CSV antes de importar.");
                setStatus("Selecione um arquivo CSV primeiro.", false);
                return;
            }
            importarCSV(arquivoSelecionado[0]);
        });

        HBox botoes = new HBox(12, btnEscolher, btnImportar);
        botoes.setPadding(new Insets(4, 0, 0, 0));

        cardUpload.getChildren().addAll(lDesc, arquivoLabel, botoes);
        pagina.getChildren().add(cardUpload);

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 4: Relatorio ──────────────────────────────────────
    private Tab criarAbaRelatorio() {
        Tab tab = new Tab("  4. Relatorio  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(criarBannerInfo(
                "O que o relatorio calcula?",
                "Total de horas trabalhadas, horas esperadas, horas extras,\n" +
                        "horas faltando e saldo do banco de horas do funcionario no periodo.",
                DICA_FUNDO, DICA_BORDA, AZUL_ACENTO
        ));

        VBox card = criarCard("Filtros do Relatorio");

        VBox campoId = criarCampoFormulario(
                "ID do Funcionario",
                "Ex: 1",
                "Para ver os IDs, va em 'Ver Todos os Funcionarios' na aba 2."
        );
        relFuncIdField = (TextField) campoId.getChildren().get(1);

        VBox campoInicio = criarCampoFormulario(
                "Data de Inicio",
                "Ex: 2025-01-01",
                "Formato: ANO-MES-DIA"
        );
        relInicioField = (TextField) campoInicio.getChildren().get(1);
        relInicioField.setText(LocalDate.now().withDayOfMonth(1).toString());

        VBox campoFim = criarCampoFormulario(
                "Data de Fim",
                "Ex: 2025-01-31",
                "Formato: ANO-MES-DIA"
        );
        relFimField = (TextField) campoFim.getChildren().get(1);
        relFimField.setText(LocalDate.now().toString());

        HBox botoes = new HBox(12);
        botoes.setPadding(new Insets(8, 0, 0, 0));
        Button btnGerar = botaoPrimario("Gerar Relatorio");
        btnGerar.setOnAction(e -> gerarRelatorio());
        Button btnGeral = botaoSecundario("Relatorio de Todos");
        btnGeral.setOnAction(e -> gerarRelatorioGeral());
        botoes.getChildren().addAll(btnGerar, btnGeral);

        Label lDica = new Label("'Relatorio de Todos' exibe todos os funcionarios no periodo informado.");
        lDica.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");

        card.getChildren().addAll(campoId, separador(), campoInicio, separador(), campoFim, botoes, lDica);
        pagina.getChildren().add(card);

        resultadoBox = new VBox(0);
        resultadoBox.setStyle("-fx-background-color: " + FUNDO + ";");
        pagina.getChildren().add(resultadoBox);

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 5: Escala Automatica ──────────────────────────────
    private Tab criarAbaEscala() {
        Tab tab = new Tab("  5. Escala  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(criarBannerInfo(
                "Geracao Automatica de Escala",
                "Informe o periodo, os horarios e o setor para gerar\n" +
                        "a escala de trabalho automaticamente para sua equipe.",
                DICA_FUNDO, DICA_BORDA, AZUL_ACENTO
        ));

        VBox card = criarCard("Configurar Escala de Trabalho");

        // Linha 1: Periodo
        Label lPeriodo = new Label("Periodo");
        lPeriodo.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox linhaPeriodo = new HBox(16);

        VBox campoInicio = criarCampoFormulario("Data Inicio", "2025-01-01", "Inicio da escala");
        escalaInicioField = (TextField) campoInicio.getChildren().get(1);
        HBox.setHgrow(campoInicio, Priority.ALWAYS);

        VBox campoFim = criarCampoFormulario("Data Fim", "2025-01-31", "Fim da escala");
        escalaFimField = (TextField) campoFim.getChildren().get(1);
        HBox.setHgrow(campoFim, Priority.ALWAYS);

        linhaPeriodo.getChildren().addAll(campoInicio, campoFim);

        // Linha 2: Horarios
        Label lHorarios = new Label("Horarios");
        lHorarios.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox linhaHorarios = new HBox(16);

        VBox campoAbertura = criarCampoFormulario("Horario de Abertura", "08:00", "Horario de entrada");
        escalaAberturaField = (TextField) campoAbertura.getChildren().get(1);
        HBox.setHgrow(campoAbertura, Priority.ALWAYS);

        VBox campoFechamento = criarCampoFormulario("Horario de Fechamento", "18:00", "Horario de saida");
        escalaFechamentoField = (TextField) campoFechamento.getChildren().get(1);
        HBox.setHgrow(campoFechamento, Priority.ALWAYS);

        linhaHorarios.getChildren().addAll(campoAbertura, campoFechamento);

        // Linha 3: Horas e Setor
        HBox linhaExtra = new HBox(16);

        VBox campoHoras = criarCampoFormulario("Horas por Turno", "8", "Quantidade de horas do turno");
        escalaHorasTurnoField = (TextField) campoHoras.getChildren().get(1);
        HBox.setHgrow(campoHoras, Priority.ALWAYS);

        VBox campoSetor = criarCampoFormulario("Setor", "TI", "Nome do setor ou departamento");
        escalaSetorField = (TextField) campoSetor.getChildren().get(1);
        HBox.setHgrow(campoSetor, Priority.ALWAYS);

        linhaExtra.getChildren().addAll(campoHoras, campoSetor);

        Button btnGerar = botaoCor("Gerar Escala Automaticamente", "#8E44AD", "#6C3483");
        btnGerar.setOnAction(e -> gerarEscalaAutomatica());
        btnGerar.setPrefWidth(280);

        card.getChildren().addAll(
                lPeriodo, linhaPeriodo, separador(),
                lHorarios, linhaHorarios, separador(),
                linhaExtra,
                new Label(""),
                btnGerar
        );
        pagina.getChildren().add(card);

        pagina.getChildren().add(criarBannerInfo(
                "Escalas Fixas Disponiveis",
                "O sistema ja vem com escalas prontas:\n" +
                        "Segunda a Sexta  |  Segunda a Sabado  |  12x36  |  6x2  |  Final de Semana\n\n" +
                        "Atribua uma escala ao funcionario no cadastro (campo ID Escala).",
                SUCESSO_FUNDO, SUCESSO_BORDA, VERDE
        ));

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Barra inferior com console ────────────────────────────
    private VBox criarBarraInferior() {
        HBox cabConsole = new HBox();
        cabConsole.setPadding(new Insets(5, 12, 5, 12));
        cabConsole.setAlignment(Pos.CENTER_LEFT);
        cabConsole.setStyle("-fx-background-color: #1C2833; -fx-border-color: #2C3E50; -fx-border-width: 1 0 0 0;");

        Label lConsole = new Label("Console de Atividades");
        lConsole.setStyle("-fx-text-fill: #5D8AA8; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setStyle(
                "-fx-background-color: #2C3E50;" +
                        "-fx-text-fill: #5D8AA8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-padding: 3 10;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        btnLimpar.setOnAction(e -> logArea.clear());

        cabConsole.getChildren().addAll(lConsole, esp, btnLimpar);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(110);
        logArea.setStyle(
                "-fx-control-inner-background: #0D1117;" +
                        "-fx-text-fill: #58D68D;" +
                        "-fx-font-family: monospace;" +
                        "-fx-font-size: 11px;"
        );

        statusBar = new Label("  Pronto.");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setStyle(
                "-fx-background-color: " + VERDE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 12;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        return new VBox(cabConsole, logArea, statusBar);
    }

    // ── Chamadas a API ────────────────────────────────────────
    private void criarCargo() {
        String nome  = cargoNomeField.getText().trim();
        String horas = horasField.getText().trim();
        if (nome.isEmpty() || horas.isEmpty()) {
            log("ERRO: Preencha o nome do cargo e as horas diarias.");
            setStatus("Preencha todos os campos.", false);
            return;
        }
        try { Double.parseDouble(horas); } catch (NumberFormatException ex) {
            log("ERRO: Horas invalidas. Use formato numerico: 8.0");
            setStatus("Horas invalidas.", false);
            return;
        }
        String json = String.format("{\"nome\":\"%s\",\"horasDiarias\":%s}", nome, horas);
        post("/cargos", json, "Cargo '" + nome + "' salvo com sucesso!");
        cargoNomeField.clear();
        horasField.clear();
    }

    private void listarCargos() {
        get("/cargos", "Lista de cargos carregada.");
    }

    private void criarFuncionario() {
        String nome      = nomeField.getText().trim();
        String matricula = matriculaField.getText().trim();
        String cargoId   = cargoIdField.getText().trim();
        if (nome.isEmpty() || matricula.isEmpty() || cargoId.isEmpty()) {
            log("ERRO: Preencha nome, matricula e ID do cargo.");
            setStatus("Preencha todos os campos.", false);
            return;
        }
        String json = String.format(
                "{\"nome\":\"%s\",\"matricula\":\"%s\",\"cargo\":{\"id\":%s}}",
                nome, matricula, cargoId);
        post("/funcionarios", json, "Funcionario '" + nome + "' salvo!");
        nomeField.clear(); matriculaField.clear(); cargoIdField.clear();
    }

    private void listarFuncionarios() {
        get("/funcionarios", "Lista de funcionarios carregada.");
    }

    private void importarCSV(File arquivo) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    log("Enviando: " + arquivo.getName() + "...");
                    setStatus("Importando CSV...", true);
                });
                String boundary = "Boundary-" + System.currentTimeMillis();
                byte[] bytes = Files.readAllBytes(arquivo.toPath());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\""
                        + arquivo.getName() + "\"\r\nContent-Type: text/csv\r\n\r\n").getBytes());
                out.write(bytes);
                out.write(("\r\n--" + boundary + "--\r\n").getBytes());
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + "/ponto/importar"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                        .build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    log("Importacao concluida:\n" + resp.body());
                    setStatus("Arquivo importado com sucesso!", true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO ao importar: " + ex.getMessage());
                    setStatus("Erro ao importar. Veja o console.", false);
                });
            }
        }).start();
    }

    private void gerarRelatorio() {
        String id     = relFuncIdField.getText().trim();
        String inicio = relInicioField.getText().trim();
        String fim    = relFimField.getText().trim();
        if (id.isEmpty()) { log("ERRO: Informe o ID do funcionario."); setStatus("Informe o ID.", false); return; }
        setStatus("Calculando relatorio...", true);
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + "/ponto/relatorio?funcionarioId=" + id + "&inicio=" + inicio + "&fim=" + fim))
                        .GET().build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    log("Relatorio:\n" + resp.body());
                    exibirResultado(resp.body());
                    setStatus("Relatorio gerado!", true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> { log("ERRO: " + ex.getMessage()); setStatus("Erro.", false); });
            }
        }).start();
    }

    private void gerarRelatorioGeral() {
        String inicio = relInicioField.getText().trim();
        String fim    = relFimField.getText().trim();
        setStatus("Calculando relatorio geral...", true);
        get("/ponto/relatorio/geral?inicio=" + inicio + "&fim=" + fim, "Relatorio geral gerado!");
    }

    private void gerarEscalaAutomatica() {
        String json = String.format(
                "{\"dataInicio\":\"%s\",\"dataFim\":\"%s\",\"horarioAbertura\":\"%s\"," +
                        "\"horarioFechamento\":\"%s\",\"horasPorTurno\":%s,\"setor\":\"%s\"}",
                escalaInicioField.getText().trim(),
                escalaFimField.getText().trim(),
                escalaAberturaField.getText().trim(),
                escalaFechamentoField.getText().trim(),
                escalaHorasTurnoField.getText().trim(),
                escalaSetorField.getText().trim()
        );
        post("/escala/gerar", json, "Escala gerada com sucesso!");
    }

    // Exibe resultado do relatorio em cards visuais
    private void exibirResultado(String json) {
        resultadoBox.getChildren().clear();
        VBox card = criarCard("Resultado do Relatorio");

        String[][] campos = {
                {"funcionario", "Funcionario"},
                {"matricula",   "Matricula"},
                {"cargo",       "Cargo"},
                {"escala",      "Escala"},
                {"periodo",     "Periodo"},
                {"diasTrabalhados", "Dias Trabalhados"},
                {"diasFolga",   "Dias de Folga"},
                {"diasFalta",   "Dias de Falta"},
                {"horasTrabalhadas", "Horas Trabalhadas"},
                {"horasEsperadas",   "Horas Esperadas"},
                {"horasExtras",      "Horas Extras"},
                {"horasFaltando",    "Horas Faltando"},
                {"saldoBancoHoras",  "Saldo Banco de Horas"},
                {"status",           "Situacao"},
        };

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(8, 0, 0, 0));

        int linha = 0;
        for (String[] campo : campos) {
            String valor = extrairJson(json, campo[0]);
            if (valor.isEmpty()) continue;

            Label lRotulo = new Label(campo[1] + ":");
            lRotulo.setMinWidth(200);
            lRotulo.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            String cor = TEXTO;
            if (campo[0].equals("horasExtras") && !valor.startsWith("0.00"))   cor = LARANJA;
            if (campo[0].equals("horasFaltando") && !valor.startsWith("0.00")) cor = VERMELHO;
            if (campo[0].equals("status") && valor.contains("extras"))         cor = LARANJA;
            if (campo[0].equals("status") && valor.contains("cumprida"))       cor = VERDE;
            if (campo[0].equals("status") && valor.contains("faltando"))       cor = VERMELHO;
            if (campo[0].equals("saldoBancoHoras"))                             cor = AZUL_ACENTO;

            Label lValor = new Label(valor);
            lValor.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 13px;" +
                    (campo[0].equals("status") ? " -fx-font-weight: bold;" : ""));

            grid.add(lRotulo, 0, linha);
            grid.add(lValor, 1, linha);
            linha++;

            if (campo[0].equals("periodo") || campo[0].equals("horasEsperadas") || campo[0].equals("diasFalta")) {
                Separator sep = new Separator();
                GridPane.setColumnSpan(sep, 2);
                grid.add(sep, 0, linha++);
            }
        }

        card.getChildren().add(grid);
        card.setStyle(card.getStyle() +
                "-fx-border-color: " + SUCESSO_BORDA + "; -fx-border-width: 2; -fx-border-radius: 8;");
        resultadoBox.getChildren().add(card);
    }

    private String extrairJson(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int idx = json.indexOf(busca);
        if (idx == -1) return "";
        int inicio = idx + busca.length();
        char c = json.charAt(inicio);
        if (c == '"') {
            int fim = json.indexOf('"', inicio + 1);
            return json.substring(inicio + 1, fim);
        }
        int fim = json.indexOf(',', inicio);
        if (fim == -1) fim = json.indexOf('}', inicio);
        return json.substring(inicio, fim).trim();
    }

    // ── HTTP helpers ──────────────────────────────────────────
    private void get(String path, String msgSucesso) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path)).GET().build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> { log(resp.body()); setStatus(msgSucesso, true); });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO: " + ex.getMessage());
                    setStatus("Erro de comunicacao. O servidor esta rodando?", false);
                });
            }
        }).start();
    }

    private void post(String path, String json, String msgSucesso) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> { log(resp.body()); setStatus(msgSucesso, true); });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO: " + ex.getMessage());
                    setStatus("Erro de comunicacao.", false);
                });
            }
        }).start();
    }

    // ── UI helpers ────────────────────────────────────────────

    private VBox criarCard(String titulo) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + FUNDO_CARD + ";" +
                        "-fx-border-color: " + BORDA + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"
        );
        Label lTitulo = new Label(titulo);
        lTitulo.setStyle(
                "-fx-text-fill: " + AZUL_MEDIO + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        card.getChildren().addAll(lTitulo, new Separator());
        return card;
    }

    private VBox criarCampoFormulario(String rotulo, String placeholder, String ajuda) {
        VBox box = new VBox(4);
        Label lRotulo = new Label(rotulo);
        lRotulo.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setMaxWidth(340);
        campo.setStyle(
                "-fx-background-color: #FDFEFE;" +
                        "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-border-color: " + BORDA + ";" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 7 10;" +
                        "-fx-font-size: 12px;"
        );
        Label lAjuda = new Label(ajuda);
        lAjuda.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px;");
        lAjuda.setWrapText(true);
        box.getChildren().addAll(lRotulo, campo, lAjuda);
        return box;
    }

    private VBox criarBannerInfo(String titulo, String texto, String fundo, String borda, String corTitulo) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle(
                "-fx-background-color: " + fundo + ";" +
                        "-fx-border-color: " + borda + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        Label lTitulo = new Label(titulo);
        lTitulo.setStyle("-fx-text-fill: " + corTitulo + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label lTexto = new Label(texto);
        lTexto.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 11px;");
        lTexto.setWrapText(true);
        box.getChildren().addAll(lTitulo, lTexto);
        return box;
    }

    private Button botaoPrimario(String texto) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: " + AZUL_ACENTO + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 9 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private Button botaoSecundario(String texto) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: " + AZUL_ACENTO + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8 18;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: " + AZUL_ACENTO + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.80));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private Button botaoCor(String texto, String cor, String corHover) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: " + cor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 9 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(cor, corHover)));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace(corHover, cor)));
        return b;
    }

    private Separator separador() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: " + BORDA + ";");
        return s;
    }

    private void log(String msg) {
        Platform.runLater(() -> {
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + hora + "] " + msg + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void setStatus(String texto, boolean sucesso) {
        Platform.runLater(() -> {
            statusBar.setText("  " + texto);
            String cor = sucesso ? VERDE : VERMELHO;
            statusBar.setStyle(
                    "-fx-background-color: " + cor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 5 12;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
            );
            if (statusDot != null)
                statusDot.setFill(sucesso ? Color.web("#2ECC71") : Color.web("#E74C3C"));
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}