package com.empresa.controle_horas.Ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Aba de Escala por Funcionario.
 *
 * Como adicionar ao MainApp:
 *
 *   EscalaFuncionarioView escalaView =
 *       new EscalaFuncionarioView(http, logArea, statusBar, statusDot);
 *   abas.getTabs().add(escalaView.criarAba());
 */
public class EscalaFuncionarioView {

    private static final String API = "http://localhost:8080/api";

    private final HttpClient                  http;
    private final TextArea                    logArea;
    private final Label                       statusBar;
    private final javafx.scene.shape.Circle   statusDot;

    // Cores
    private static final String AZUL_MEDIO    = "#1A3C5E";
    private static final String AZUL_ACENTO   = "#2E86C1";
    private static final String VERDE         = "#1E8449";
    private static final String VERDE_CLARO   = "#27AE60";
    private static final String VERMELHO      = "#C0392B";
    private static final String LARANJA       = "#D35400";
    private static final String ROXO          = "#6C3483";
    private static final String FUNDO         = "#EEF2F7";
    private static final String FUNDO_CARD    = "#FFFFFF";
    private static final String BORDA         = "#D5DCE8";
    private static final String TEXTO         = "#1C2833";
    private static final String TEXTO_FRACO   = "#6C7A89";
    private static final String DICA_FUNDO    = "#EBF5FB";
    private static final String DICA_BORDA    = "#AED6F1";
    private static final String SUCESSO_FUNDO = "#EAFAF1";
    private static final String SUCESSO_BORDA = "#A9DFBF";
    private static final String COR_DIA_UTIL  = "#1A3C5E";
    private static final String COR_SABADO    = "#1A5276";
    private static final String COR_DOMINGO   = "#6C3483";

    // Dados dos dias
    private static final String[] DIAS_SEMANA = {
            "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"
    };
    private static final String[] DIAS_NOMES = {
            "Segunda","Terca","Quarta","Quinta","Sexta","Sabado","Domingo"
    };

    // Controles
    private TextField      funcIdField;
    private Label          funcNomeLabel;
    private Label          funcCargoLabel;
    private Label          funcHorasLabel;
    private Label          resumoLabel;
    private VBox           diasContainer;
    private HBox           infoBox;

    private final ToggleButton[] diaToggle     = new ToggleButton[7];
    private final TextField[]    diaEntrada    = new TextField[7];
    private final TextField[]    diaSaida      = new TextField[7];
    private final Label[]        diaHorasLabel = new Label[7];
    private final VBox[]         diaCard       = new VBox[7];

    private double horasDiariasCargo  = 8.0;
    private Long   funcionarioIdAtual = null;

    public EscalaFuncionarioView(
            HttpClient http,
            TextArea logArea,
            Label statusBar,
            javafx.scene.shape.Circle statusDot) {
        this.http      = http;
        this.logArea   = logArea;
        this.statusBar = statusBar;
        this.statusDot = statusDot;
    }

    // ── Cria a aba ────────────────────────────────────────────
    public Tab criarAba() {
        Tab tab = new Tab("  6. Escala Automatica  ");

        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(24));
        pagina.setStyle("-fx-background-color: " + FUNDO + ";");

        pagina.getChildren().add(banner(
                "Escala por Funcionario",
                "Selecione um funcionario, escolha os dias da semana que ele trabalha " +
                        "e configure os horarios de entrada e saida de cada dia.\n" +
                        "As horas por turno sao calculadas automaticamente com base na carga horaria do cargo.\n\n" +
                        "Exemplo: cargo de 8h48min/dia (44h/semana):\n" +
                        "  Em 5 dias (5x2) -> 8h48min por dia\n" +
                        "  Em 6 dias (6x1) -> 7h20min por dia\n" +
                        "  Em 4 dias       -> 11h por dia",
                DICA_FUNDO, DICA_BORDA
        ));

        pagina.getChildren().add(cardSelecao());

        diasContainer = new VBox(12);
        diasContainer.setVisible(false);
        diasContainer.setManaged(false);
        diasContainer.getChildren().addAll(
                cardDias(),
                cardResumo(),
                cardBotoes()
        );
        pagina.getChildren().add(diasContainer);

        ScrollPane scroll = new ScrollPane(pagina);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FUNDO + "; -fx-background-color: " + FUNDO + ";");
        tab.setContent(scroll);
        return tab;
    }

    // ── Card: selecao do funcionario ──────────────────────────
    private VBox cardSelecao() {
        VBox card = card("1. Selecionar Funcionario");

        HBox linhaBusca = new HBox(12);
        linhaBusca.setAlignment(Pos.CENTER_LEFT);

        Label lId = new Label("ID do Funcionario:");
        lId.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 140;");

        funcIdField = new TextField();
        funcIdField.setPromptText("Ex: 1");
        funcIdField.setPrefWidth(100);
        funcIdField.setStyle(estiloTf());

        Button btnBuscar = botaoPrimario("Buscar");
        btnBuscar.setOnAction(e -> buscarFuncionario());

        Button btnVerTodos = botaoSecundario("Ver Todos");
        btnVerTodos.setOnAction(e -> get("/funcionarios", "Funcionarios listados."));

        linhaBusca.getChildren().addAll(lId, funcIdField, btnBuscar, btnVerTodos);

        // Painel de info (inicialmente oculto)
        infoBox = new HBox(24);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));
        infoBox.setStyle(
                "-fx-background-color: " + SUCESSO_FUNDO + ";" +
                        "-fx-border-color: " + SUCESSO_BORDA + ";" +
                        "-fx-border-radius: 6; -fx-background-radius: 6;"
        );
        infoBox.setVisible(false);
        infoBox.setManaged(false);

        funcNomeLabel = infoLabel("Funcionario");
        funcCargoLabel = infoLabel("Cargo");
        funcCargoLabel.setStyle(funcCargoLabel.getStyle()
                .replace(TEXTO, AZUL_ACENTO));
        funcHorasLabel = infoLabel("Carga Horaria/Dia");
        funcHorasLabel.setStyle(funcHorasLabel.getStyle()
                .replace(TEXTO, VERDE));

        infoBox.getChildren().addAll(
                blocoInfo("Funcionario", funcNomeLabel),
                new Separator(),
                blocoInfo("Cargo", funcCargoLabel),
                new Separator(),
                blocoInfo("Horas por Dia", funcHorasLabel)
        );

        card.getChildren().addAll(linhaBusca, infoBox);
        return card;
    }

    private Label infoLabel(String placeholder) {
        Label l = new Label(placeholder);
        l.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    private VBox blocoInfo(String rotulo, Label valor) {
        VBox b = new VBox(2);
        Label lR = new Label(rotulo);
        lR.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px;");
        b.getChildren().addAll(lR, valor);
        return b;
    }

    // ── Card: dias da semana ──────────────────────────────────
    private VBox cardDias() {
        VBox card = card("2. Configurar Dias e Horarios");

        Label lDesc = new Label(
                "Clique em um dia para ativar ou desativar. " +
                        "Quando ativado, informe os horarios de entrada e saida."
        );
        lDesc.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");

        // Legenda
        HBox legenda = new HBox(16);
        legenda.setAlignment(Pos.CENTER_LEFT);
        legenda.setPadding(new Insets(4, 0, 8, 0));
        for (String[] item : new String[][]{
                {COR_DIA_UTIL, "Dia Util"},
                {COR_SABADO,   "Sabado"},
                {COR_DOMINGO,  "Domingo"},
        }) {
            Rectangle rect = new Rectangle(12, 12);
            rect.setFill(Color.web(item[0]));
            rect.setArcWidth(4); rect.setArcHeight(4);
            Label lL = new Label(item[1]);
            lL.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px;");
            HBox leg = new HBox(6, rect, lL);
            leg.setAlignment(Pos.CENTER_LEFT);
            legenda.getChildren().add(leg);
        }

        // Cards dos dias em linha
        HBox gridDias = new HBox(10);
        gridDias.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 7; i++) {
            diaCard[i] = criarDiaCard(i);
            gridDias.getChildren().add(diaCard[i]);
        }

        // Presets rapidos
        HBox presets = new HBox(8);
        presets.setAlignment(Pos.CENTER_LEFT);
        presets.setPadding(new Insets(8, 0, 0, 0));

        Label lPresets = new Label("Presets:");
        lPresets.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");

        presets.getChildren().addAll(
                lPresets,
                botaoMini("5x2 Seg-Sex",    VERDE,      new boolean[]{true,true,true,true,true,false,false}),
                botaoMini("6x1 Seg-Sab",    COR_SABADO, new boolean[]{true,true,true,true,true,true,false}),
                botaoMini("Ter-Dom",         COR_DOMINGO,new boolean[]{false,true,true,true,true,false,true}),
                botaoMini("Sab-Dom",         ROXO,       new boolean[]{false,false,false,false,false,true,true}),
                botaoMini("Limpar",          VERMELHO,   new boolean[]{false,false,false,false,false,false,false})
        );

        card.getChildren().addAll(lDesc, legenda, gridDias, presets);
        return card;
    }

    // ── Card individual de um dia ─────────────────────────────
    private VBox criarDiaCard(int idx) {
        String corBase = idx == 6 ? COR_DOMINGO : (idx == 5 ? COR_SABADO : COR_DIA_UTIL);

        VBox card = new VBox(8);
        card.setPrefWidth(128);
        card.setMinWidth(128);
        card.setPadding(new Insets(10, 8, 10, 8));
        card.setStyle(estiloCardInativo());

        // Toggle button
        diaToggle[idx] = new ToggleButton(DIAS_NOMES[idx]);
        diaToggle[idx].setMaxWidth(Double.MAX_VALUE);
        diaToggle[idx].setStyle(estiloToggleInativo());

        // Campos de horario
        VBox campos = new VBox(5);
        campos.setVisible(false);
        campos.setManaged(false);

        Label lE = new Label("Entrada");
        lE.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px;");
        diaEntrada[idx] = new TextField("08:00");
        diaEntrada[idx].setStyle(estiloTf() + "-fx-font-size: 11px; -fx-padding: 4 6;");
        diaEntrada[idx].setMaxWidth(Double.MAX_VALUE);

        Label lS = new Label("Saida");
        lS.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 10px;");
        diaSaida[idx] = new TextField("17:00");
        diaSaida[idx].setStyle(estiloTf() + "-fx-font-size: 11px; -fx-padding: 4 6;");
        diaSaida[idx].setMaxWidth(Double.MAX_VALUE);

        campos.getChildren().addAll(lE, diaEntrada[idx], lS, diaSaida[idx]);

        // Label de horas
        diaHorasLabel[idx] = new Label("Folga");
        diaHorasLabel[idx].setMaxWidth(Double.MAX_VALUE);
        diaHorasLabel[idx].setAlignment(Pos.CENTER);
        diaHorasLabel[idx].setStyle(
                "-fx-text-fill: " + TEXTO_FRACO + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-alignment: center;"
        );

        final int i = idx;

        // Listener do toggle
        diaToggle[idx].selectedProperty().addListener((o, old, ativo) -> {
            campos.setVisible(ativo);
            campos.setManaged(ativo);
            if (ativo) {
                diaToggle[i].setStyle(estiloToggleAtivo(corBase));
                diaCard[i].setStyle(estiloCardAtivo(corBase));
                recalcularHoraDia(i);
            } else {
                diaToggle[i].setStyle(estiloToggleInativo());
                diaCard[i].setStyle(estiloCardInativo());
                diaHorasLabel[i].setText("Folga");
                diaHorasLabel[i].setStyle(
                        "-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");
            }
            recalcularResumo();
        });

        // Recalcula ao digitar horario
        diaEntrada[idx].textProperty().addListener((o, old, v) -> recalcularHoraDia(i));
        diaSaida[idx].textProperty().addListener((o, old, v) -> recalcularHoraDia(i));

        card.getChildren().addAll(diaToggle[idx], new Separator(), campos, diaHorasLabel[idx]);
        return card;
    }

    // ── Card: resumo ──────────────────────────────────────────
    private VBox cardResumo() {
        VBox card = card("3. Resumo da Escala");
        resumoLabel = new Label("Configure os dias acima para ver o resumo aqui.");
        resumoLabel.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 12px;");
        resumoLabel.setWrapText(true);
        card.getChildren().add(resumoLabel);
        return card;
    }

    // ── Card: botoes ──────────────────────────────────────────
    private VBox cardBotoes() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: " + FUNDO_CARD + ";" +
                        "-fx-border-color: " + BORDA + ";" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_LEFT);

        Button btnSalvar = botaoCor("Salvar Escala", VERDE, "#145A32");
        btnSalvar.setOnAction(e -> salvarEscala());

        Button btnBuscar = botaoSecundario("Ver Escala Salva");
        btnBuscar.setOnAction(e -> buscarEscalaSalva());

        botoes.getChildren().addAll(btnSalvar, btnBuscar);

        Label lDica = new Label(
                "Dica: Ao salvar, a escala anterior do funcionario e substituida pela nova configuracao."
        );
        lDica.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 11px;");
        lDica.setWrapText(true);

        card.getChildren().addAll(botoes, lDica);
        return card;
    }

    // ── Logica ────────────────────────────────────────────────

    private void buscarFuncionario() {
        String idStr = funcIdField.getText().trim();
        if (idStr.isEmpty()) {
            log("Informe o ID do funcionario.");
            setStatus("Informe o ID do funcionario.", false);
            return;
        }

        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + "/funcionarios/" + idStr))
                        .GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        String body  = resp.body();
                        String nome  = extrairJson(body, "nome");
                        String cargo = extrairJsonAninhado(body, "cargo", "nome");
                        String horas = extrairJsonAninhado(body, "cargo", "horasDiarias");

                        funcNomeLabel.setText(nome.isEmpty() ? "?" : nome);
                        funcCargoLabel.setText(cargo.isEmpty() ? "?" : cargo);

                        if (!horas.isEmpty()) {
                            double h = Double.parseDouble(horas);
                            horasDiariasCargo = h;
                            int hrs = (int) h;
                            int min = (int) Math.round((h - hrs) * 60);
                            funcHorasLabel.setText(hrs + "h" + (min > 0 ? min + "min" : "") + "/dia");
                        }

                        try { funcionarioIdAtual = Long.parseLong(idStr); } catch (Exception ex) {}

                        infoBox.setVisible(true);
                        infoBox.setManaged(true);
                        diasContainer.setVisible(true);
                        diasContainer.setManaged(true);

                        // Sugere horario de saida para todos os dias
                        sugerirSaidas(5);

                        log("Funcionario: " + nome + " | " + cargo + " | " + funcHorasLabel.getText());
                        setStatus("Funcionario carregado. Configure os dias de trabalho.", true);
                    } else {
                        log("Funcionario ID " + idStr + " nao encontrado.");
                        setStatus("Funcionario nao encontrado.", false);
                        infoBox.setVisible(false);
                        infoBox.setManaged(false);
                        diasContainer.setVisible(false);
                        diasContainer.setManaged(false);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("Erro: " + ex.getMessage());
                    setStatus("Erro de comunicacao.", false);
                });
            }
        }).start();
    }

    private void sugerirSaidas(int diasAtivos) {
        double horasSemanais = horasDiariasCargo * 5;
        double horasPorDia   = diasAtivos > 0 ? horasSemanais / diasAtivos : horasDiariasCargo;
        long   minutosTotais = (long)(horasPorDia * 60);
        int horasSaida = (int)(8 + minutosTotais / 60);
        int minSaida   = (int)(minutosTotais % 60);
        String saida   = String.format("%02d:%02d", horasSaida, minSaida);
        for (int i = 0; i < 7; i++) {
            if (diaSaida[i] != null) diaSaida[i].setText(saida);
        }
    }

    private Button botaoMini(String texto, String cor, boolean[] preset) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: " + cor + ";" +
                        "-fx-text-fill: white; -fx-font-size: 10px;" +
                        "-fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.80));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        b.setOnAction(e -> {
            for (int i = 0; i < 7; i++) {
                if (diaToggle[i] != null) diaToggle[i].setSelected(preset[i]);
            }
            int ativos = 0;
            for (boolean p : preset) if (p) ativos++;
            sugerirSaidas(ativos > 0 ? ativos : 5);
            recalcularResumo();
        });
        return b;
    }

    private void recalcularHoraDia(int idx) {
        try {
            String[] pE = diaEntrada[idx].getText().trim().split(":");
            String[] pS = diaSaida[idx].getText().trim().split(":");
            if (pE.length < 2 || pS.length < 2) return;

            int minE = Integer.parseInt(pE[0]) * 60 + Integer.parseInt(pE[1]);
            int minS = Integer.parseInt(pS[0]) * 60 + Integer.parseInt(pS[1]);
            int tot  = minS - minE;

            if (tot <= 0) {
                diaHorasLabel[idx].setText("Invalido");
                diaHorasLabel[idx].setStyle("-fx-text-fill: " + VERMELHO + "; -fx-font-size: 11px;");
                return;
            }

            int h = tot / 60;
            int m = tot % 60;
            diaHorasLabel[idx].setText(h + "h" + (m > 0 ? m + "min" : ""));
            diaHorasLabel[idx].setStyle(
                    "-fx-text-fill: " + VERDE + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            recalcularResumo();
        } catch (Exception ex) { /* ignorar enquanto digita */ }
    }

    private void recalcularResumo() {
        if (resumoLabel == null) return;

        int    diasAtivos = 0;
        double totalHoras = 0;
        StringBuilder sb  = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            if (diaToggle[i] == null || !diaToggle[i].isSelected()) continue;
            diasAtivos++;
            try {
                String[] pE = diaEntrada[i].getText().trim().split(":");
                String[] pS = diaSaida[i].getText().trim().split(":");
                int tot = (Integer.parseInt(pS[0]) * 60 + Integer.parseInt(pS[1]))
                        - (Integer.parseInt(pE[0]) * 60 + Integer.parseInt(pE[1]));
                if (tot > 0) {
                    double h = tot / 60.0;
                    totalHoras += h;
                    int hrs = (int) h;
                    int min = (int) Math.round((h - hrs) * 60);
                    sb.append(DIAS_NOMES[i]).append(": ")
                            .append(diaEntrada[i].getText()).append(" -> ")
                            .append(diaSaida[i].getText())
                            .append(" (").append(hrs).append("h")
                            .append(min > 0 ? min + "min" : "").append(")   ");
                }
            } catch (Exception ex) { /* ignorar */ }
        }

        if (diasAtivos == 0) {
            resumoLabel.setText("Nenhum dia selecionado.");
            resumoLabel.setStyle("-fx-text-fill: " + TEXTO_FRACO + "; -fx-font-size: 12px;");
            return;
        }

        int thH = (int) totalHoras;
        int thM = (int) Math.round((totalHoras - thH) * 60);
        double esperado = horasDiariasCargo * 5;
        double dif = totalHoras - esperado;
        String difStr = String.format("%.1f", Math.abs(dif));
        String difTipo = dif > 0.1  ? " (+" + difStr + "h extras)"
                : dif < -0.1 ? " (-" + difStr + "h abaixo do esperado)"
                  : " (exato)";

        String cor = Math.abs(dif) < 0.2 ? VERDE : dif > 0 ? LARANJA : VERMELHO;

        resumoLabel.setText(
                diasAtivos + " dias  |  "
                        + thH + "h" + (thM > 0 ? thM + "min" : "") + "/semana"
                        + difTipo + "\n" + sb
        );
        resumoLabel.setStyle(
                "-fx-text-fill: " + cor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    private void salvarEscala() {
        if (funcionarioIdAtual == null) {
            log("Busque um funcionario antes de salvar.");
            setStatus("Selecione um funcionario primeiro.", false);
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"funcionarioId\":").append(funcionarioIdAtual)
                .append(",\"dias\":[");

        for (int i = 0; i < 7; i++) {
            if (i > 0) json.append(",");
            // usa "diaAtivo" para bater com o DTO
            boolean ativo = diaToggle[i] != null && diaToggle[i].isSelected();
            json.append("{\"diaSemana\":\"").append(DIAS_SEMANA[i]).append("\"");
            json.append(",\"diaAtivo\":").append(ativo);
            if (ativo && diaEntrada[i] != null && diaSaida[i] != null) {
                json.append(",\"entrada\":\"").append(diaEntrada[i].getText().trim()).append("\"");
                json.append(",\"saida\":\"").append(diaSaida[i].getText().trim()).append("\"");
            }
            json.append("}");
        }
        json.append("]}");

        post("/escala-funcionario/salvar", json.toString(), "Escala salva!");
    }

    private void buscarEscalaSalva() {
        if (funcionarioIdAtual == null) {
            log("Busque um funcionario primeiro.");
            return;
        }
        get("/escala-funcionario/" + funcionarioIdAtual, "Escala carregada.");
    }

    // ── HTTP ──────────────────────────────────────────────────
    private void get(String path, String ok) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path)).GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> { log(resp.body()); setStatus(ok, true); });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("Erro: " + ex.getMessage()); setStatus("Erro.", false);
                });
            }
        }).start();
    }

    private void post(String path, String json, String ok) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> { log(resp.body()); setStatus(ok, true); });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("Erro: " + ex.getMessage()); setStatus("Erro.", false);
                });
            }
        }).start();
    }

    // ── JSON simples ──────────────────────────────────────────
    private String extrairJson(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int idx = json.indexOf(busca);
        if (idx == -1) return "";
        int inicio = idx + busca.length();
        if (json.charAt(inicio) == '"') {
            int fim = json.indexOf('"', inicio + 1);
            return json.substring(inicio + 1, fim);
        }
        int fim = json.indexOf(',', inicio);
        if (fim == -1) fim = json.indexOf('}', inicio);
        return fim > 0 ? json.substring(inicio, fim).trim() : "";
    }

    private String extrairJsonAninhado(String json, String obj, String chave) {
        String buscaObj = "\"" + obj + "\":{";
        int idxObj = json.indexOf(buscaObj);
        if (idxObj == -1) return "";
        int inicio = idxObj + buscaObj.length();
        int fim    = json.indexOf('}', inicio);
        return extrairJson("{" + json.substring(inicio, fim) + "}", chave);
    }

    // ── UI helpers ────────────────────────────────────────────
    private VBox card(String titulo) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));
        c.setStyle(
                "-fx-background-color: " + FUNDO_CARD + ";" +
                        "-fx-border-color: " + BORDA + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"
        );
        Label lt = new Label(titulo);
        lt.setStyle("-fx-text-fill: " + AZUL_MEDIO + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        c.getChildren().addAll(lt, new Separator());
        return c;
    }

    private VBox banner(String titulo, String texto, String fundo, String borda) {
        VBox b = new VBox(5);
        b.setPadding(new Insets(12, 16, 12, 16));
        b.setStyle(
                "-fx-background-color: " + fundo + ";" +
                        "-fx-border-color: " + borda + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;"
        );
        Label lt = new Label(titulo);
        lt.setStyle("-fx-text-fill: " + AZUL_MEDIO + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label lx = new Label(texto);
        lx.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: 11px;");
        lx.setWrapText(true);
        b.getChildren().addAll(lt, lx);
        return b;
    }

    private Button botaoPrimario(String texto) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + AZUL_ACENTO + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 9 20;" +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private Button botaoSecundario(String texto) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: white; -fx-text-fill: " + AZUL_ACENTO + ";" +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 18;" +
                "-fx-background-radius: 5; -fx-border-color: " + AZUL_ACENTO + ";" +
                "-fx-border-radius: 5; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.80));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private Button botaoCor(String texto, String cor, String corHover) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 9 20;" +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(cor, corHover)));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace(corHover, cor)));
        return b;
    }

    private String estiloTf() {
        return "-fx-background-color: #FDFEFE; -fx-text-fill: " + TEXTO + ";" +
                "-fx-border-color: " + BORDA + "; -fx-border-radius: 4; -fx-background-radius: 4;" +
                "-fx-padding: 7 10; -fx-font-size: 12px;";
    }

    private String estiloCardAtivo(String cor) {
        return "-fx-background-color: " + FUNDO_CARD + ";" +
                "-fx-border-color: " + cor + "; -fx-border-width: 2;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);";
    }

    private String estiloCardInativo() {
        return "-fx-background-color: " + FUNDO_CARD + ";" +
                "-fx-border-color: " + BORDA + "; -fx-border-width: 2;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);";
    }

    private String estiloToggleAtivo(String cor) {
        return "-fx-background-color: " + cor + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12px;" +
                "-fx-background-radius: 5; -fx-cursor: hand;";
    }

    private String estiloToggleInativo() {
        return "-fx-background-color: #F2F3F4; -fx-text-fill: " + TEXTO_FRACO + ";" +
                "-fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;" +
                "-fx-border-color: " + BORDA + "; -fx-border-radius: 5;";
    }

    private void log(String msg) {
        if (logArea == null) return;
        Platform.runLater(() -> {
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + hora + "] " + msg + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void setStatus(String texto, boolean sucesso) {
        if (statusBar == null) return;
        Platform.runLater(() -> {
            statusBar.setText("  " + texto);
            statusBar.setStyle(
                    "-fx-background-color: " + (sucesso ? VERDE : VERMELHO) + ";" +
                            "-fx-text-fill: white; -fx-padding: 5 12;" +
                            "-fx-font-size: 11px; -fx-font-weight: bold;"
            );
            if (statusDot != null)
                statusDot.setFill(sucesso ? Color.web("#2ECC71") : Color.web("#E74C3C"));
        });
    }
}