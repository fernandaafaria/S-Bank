package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// PROJETO COMPLETO EM UMA CLASSE SÓ: a lógica do banco (vetores e regras)
// e a interface (janela, telas e botões).
public class Main {

    // ---------- Cores do visual ----------
    static final Color FUNDO = new Color(0xEE, 0xF2, 0xF9);
    static final Color AZUL = new Color(0x1D, 0x4E, 0xD8);
    static final Color AZUL_CLARO = new Color(0xE4, 0xEC, 0xFC);
    static final Color TEXTO = new Color(0x14, 0x21, 0x3D);
    static final Color CINZA = new Color(0x5B, 0x64, 0x78);
    static final Color BORDA = new Color(0xDB, 0xE2, 0xEF);
    static final Color VERDE = new Color(0x16, 0xA3, 0x4A);
    static final Color VERMELHO = new Color(0xDC, 0x26, 0x26);
    static final Color BRANCO = Color.WHITE;

    // ---------- Estado do atendimento ----------
    static int c = 0;               // posição do cartão nos vetores
    static int tentativa = 1;       // tentativa de senha (1 a 3)
    static String pin = "";         // dígitos digitados
    static boolean modoSaque = true;
    static double ultimoSaque = 0;
    static String usuario = "Cliente";   // nome digitado na tela de login

    // Histórico de movimentações em memória (formato: "cartaoIndex;tipo;valor;dataHora")
    static ArrayList<String> movimentacoes = new ArrayList<>();
    static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---------- Telas e componentes que mudam ----------
    static CardLayout layout = new CardLayout();
    static JPanel telas = new JPanel(layout);
    static CardLayout layoutConteudo = new CardLayout();
    static JPanel conteudo = new JPanel(layoutConteudo);   // telas ao lado da barra lateral

    static JButton btnCartao1, btnCartao2;
    static JPanel cartao;
    static JLabel lblCartaoNome, lblNumero, lblTentativa, lblPontos, lblErroLogin;

    static JTextField txtNome;
    static JLabel lblOla, lblSaldoTitulo, lblSaldoMenu, lblMsgMenu;
    static JLabel lblAvatar, lblHomeCartao, lblHomeNumero;
    static JPanel cartaoHome;
    static JButton btnNavInicio, btnNavSaldo, btnNavDeposito, btnNavSaque, btnNavHist;
    static JLabel lblPerfilNome, lblPerfilCartao;
    static JButton btnOcultar;
    static boolean ocultarSaldo = false;

    static JTextArea areaMovimentacoesHome;
    static JTextArea areaHistoricoCompleto;

    static JButton btnTabSaque, btnTabDeposito, btnConfirmar;
    static JLabel lblSaldoOp, lblTituloValor, lblErroOp;
    static JTextField txtValor;

    static JLabel lblConfSaque, lblConfSaldo;
    static JTextArea areaRecibo;

    // =====================================================
    //                 LÓGICA DO BANCO
    // =====================================================

    // Número de cada cartão (aparece na tela e no verso do cartão impresso).
    // A mesma posição = o mesmo cartão: numeros[0] usa senhas[0] e saldos[0].
    static String[] numeros = {"0001", "0002"};
    static final String NOME_CARTAO = "Cartão SBank";
    static String[] senhas = {"1234", "0000"};
    static double[] saldos = {1000.0, 200.0};

    static boolean senhaCorreta(int c, String digitada) {
        return digitada.equals(senhas[c]);
    }

    static double lerValor(String texto) {
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                return 0;
            }
            return valor;
        } catch (Exception e) {
            return 0;
        }
    }

    static void registrarMovimentacao(int cartaoIdx, String tipo, double valor) {
        String agora = LocalDateTime.now().format(FORMATO_DATA);
        movimentacoes.add(cartaoIdx + ";" + tipo + ";" + reais(valor) + ";" + agora);
    }

    static String depositar(int c, double valor) {
        if (valor > 0) {
            saldos[c] += valor;
            registrarMovimentacao(c, "Depósito", valor);
            return "";
        }
        return "Erro: Valor inválido.";
    }

    static String sacar(int c, double valor) {
        if (valor > saldos[c]) {
            return "Erro: Saldo insuficiente.";
        } else if (valor <= 0) {
            return "Erro: Valor inválido.";
        }
        saldos[c] -= valor;
        registrarMovimentacao(c, "Saque", valor);
        return "";
    }

    static String reais(double valor) {
        return String.format("R$ %.2f", valor);
    }

    static String gravarComprovante(int c, double saque, String usuario) throws IOException {
        String comprovante = "SENAI BANK - COMPROVANTE\n"
                + "Cliente: " + usuario + "\n"
                + "Cartão: " + numeros[c] + "\n"
                + "Saque: " + reais(saque) + "\n"
                + "Saldo atual: " + reais(saldos[c]);

        FileWriter arquivo = new FileWriter("comprovante_senai_bank.txt");
        arquivo.write(comprovante);
        arquivo.close();

        BufferedReader leitor = new BufferedReader(new FileReader("comprovante_senai_bank.txt"));
        String conteudo = "";
        String linha = leitor.readLine();
        while (linha != null) {
            conteudo += linha + "\n";
            linha = leitor.readLine();
        }
        leitor.close();
        return conteudo;
    }

    // =====================================================
    //                 INTERFACE (JANELA)
    // =====================================================
    static void montarTelas() {
        telas.setBackground(FUNDO);
        conteudo.setBackground(FUNDO);
        conteudo.add(criarInicio(), "menu");
        conteudo.add(centralizar(criarOperacao(), 440, 600), "valor");
        conteudo.add(centralizar(criarConfirmacao(), 440, 600), "conf");
        conteudo.add(centralizar(criarRecibo(), 440, 600), "recibo");
        conteudo.add(criarTelaHistorico(), "historico");

        JPanel app = new JPanel(new BorderLayout());   // barra lateral + conteúdo
        app.add(criarSidebar(), BorderLayout.WEST);
        app.add(conteudo, BorderLayout.CENTER);

        telas.add(centralizar(criarLogin(), 400, 720), "login");
        telas.add(app, "app");
        telas.add(centralizar(criarBloqueio(), 400, 420), "bloq");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Senai Bank");
            montarTelas();

            janela.add(telas);
            janela.setSize(1200, 820);
            janela.setExtendedState(JFrame.MAXIMIZED_BOTH); // Abre em tela inteira
            janela.setLocationRelativeTo(null);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            atualizarLogin();
            janela.setVisible(true);
        });
    }

    // =============== TELA 1: LOGIN ===============
    static JPanel criarLogin() {
        JPanel p = tela();
        p.add(topo());
        p.add(espaco(12));

        JPanel abas = new JPanel(new GridLayout(1, 2, 8, 0));
        abas.setOpaque(false);
        abas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        abas.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCartao1 = botao("Cartão " + numeros[0], AZUL, BRANCO);
        btnCartao2 = botao("Cartão " + numeros[1], BRANCO, TEXTO);
        btnCartao1.addActionListener(e -> escolherCartao(0));
        btnCartao2.addActionListener(e -> escolherCartao(1));
        abas.add(btnCartao1);
        abas.add(btnCartao2);
        p.add(abas);
        p.add(espaco(12));

        cartao = arredondado(AZUL, AZUL, 24);
        cartao.setLayout(new BorderLayout());
        cartao.setBorder(new EmptyBorder(16, 18, 16, 18));
        cartao.setPreferredSize(new Dimension(340, 150));
        cartao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        cartao.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel marca = new JLabel("SENAI BANK");
        marca.setFont(new Font("SansSerif", Font.BOLD, 16));
        marca.setForeground(BRANCO);
        lblCartaoNome = new JLabel();
        lblCartaoNome.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblCartaoNome.setForeground(BRANCO);
        lblNumero = new JLabel();
        lblNumero.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblNumero.setForeground(BRANCO);
        cartao.add(marca, BorderLayout.NORTH);
        cartao.add(lblCartaoNome, BorderLayout.CENTER);
        cartao.add(lblNumero, BorderLayout.SOUTH);
        p.add(cartao);
        p.add(espaco(12));

        p.add(rot("Nome de usuário (opcional)", 13, Font.PLAIN, CINZA));
        p.add(espaco(4));
        txtNome = new JTextField();
        txtNome.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txtNome.setHorizontalAlignment(JTextField.CENTER);
        txtNome.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(6, 12, 6, 12)));
        txtNome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(txtNome);
        p.add(espaco(12));

        p.add(rot("Digite sua senha de 4 dígitos", 16, Font.BOLD, TEXTO));
        lblTentativa = rot(" ", 13, Font.PLAIN, CINZA);
        p.add(lblTentativa);
        lblPontos = rot(" ", 26, Font.PLAIN, AZUL);
        p.add(lblPontos);
        lblErroLogin = rot(" ", 14, Font.BOLD, VERMELHO);
        p.add(lblErroLogin);
        p.add(espaco(6));

        JPanel teclado = new JPanel(new GridLayout(4, 3, 10, 10));
        teclado.setOpaque(false);
        teclado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        teclado.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] teclas = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "←"};
        for (String t : teclas) {
            if (t.isEmpty()) {
                teclado.add(new JLabel());
            } else {
                JButton b = botao(t, BRANCO, TEXTO);
                b.setFont(new Font("SansSerif", Font.BOLD, 20));
                b.addActionListener(e -> digitar(t));
                teclado.add(b);
            }
        }
        p.add(teclado);
        return p;
    }

    static void escolherCartao(int i) {
        c = i;
        tentativa = 1;
        pin = "";
        atualizarLogin();
        lblErroLogin.setText(" ");
    }

    static void atualizarLogin() {
        cores(btnCartao1, c == 0 ? AZUL : BRANCO, c == 0 ? BRANCO : TEXTO);
        cores(btnCartao2, c == 1 ? AZUL : BRANCO, c == 1 ? BRANCO : TEXTO);
        cartao.putClientProperty("cor1", new Color(0x0B, 0x3A, 0x9E));   // azul do cartão SBank
        cartao.putClientProperty("cor2", new Color(0x25, 0x63, 0xEB));
        lblCartaoNome.setText(NOME_CARTAO);
        lblNumero.setText("•••• •••• •••• " + numeros[c]);
        lblTentativa.setText("Tentativa " + tentativa + " de 3");
        int n = pin.length();
        lblPontos.setText("● ".repeat(n) + "○ ".repeat(4 - n));

        cartao.repaint();
    }

    static void digitar(String t) {
        if (t.equals("←")) {
            if (pin.length() > 0) {
                pin = pin.substring(0, pin.length() - 1);
            }
        } else if (pin.length() < 4) {
            pin += t;
        }
        atualizarLogin();

        if (pin.length() == 4) {
            if (senhaCorreta(c, pin)) {
                usuario = txtNome.getText().trim();
                if (usuario.isEmpty()) {
                    usuario = "Cliente";
                }
                if (usuario.length() > 18) {
                    usuario = usuario.substring(0, 18);
                }
                pin = "";
                lblErroLogin.setText(" ");
                abrirMenu("");
            } else {
                pin = "";
                tentativa++;
                if (tentativa > 3) {
                    mostrar("bloq");
                } else {
                    atualizarLogin();
                    lblErroLogin.setText("Senha incorreta!");
                }
            }
        }
    }

    // =============== TELA 2: INÍCIO (expandida + movimentações) ===============
    static JPanel criarInicio() {
        JPanel p = new JPanel(new BorderLayout(0, 22));
        p.setBackground(FUNDO);
        p.setBorder(new EmptyBorder(32, 42, 32, 42));

        // ----- Parte de cima: saudação e saldo (esquerda) | avatar e cartão (direita)
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);

        JPanel esq = new JPanel();
        esq.setOpaque(false);
        esq.setLayout(new BoxLayout(esq, BoxLayout.Y_AXIS));
        esq.add(esquerda(rot("Bem-vindo(a) ao seu painel", 15, Font.PLAIN, CINZA)));
        esq.add(espaco(4));
        lblOla = esquerda(rot(" ", 32, Font.BOLD, TEXTO));
        esq.add(lblOla);
        esq.add(espaco(18));

        JPanel saldo = arredondado(AZUL, new Color(0x25, 0x63, 0xEB), 24);
        saldo.setLayout(new BoxLayout(saldo, BoxLayout.Y_AXIS));
        saldo.setBorder(new EmptyBorder(16, 22, 16, 22));
        saldo.setPreferredSize(new Dimension(380, 112));
        saldo.setMaximumSize(new Dimension(380, 112));
        saldo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel linhaSaldo = new JPanel(new BorderLayout());
        linhaSaldo.setOpaque(false);
        linhaSaldo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSaldoTitulo = new JLabel(" ");
        lblSaldoTitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSaldoTitulo.setForeground(BRANCO);
        btnOcultar = new JButton("Ocultar");
        btnOcultar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnOcultar.setForeground(BRANCO);
        btnOcultar.setContentAreaFilled(false);
        btnOcultar.setBorderPainted(false);
        btnOcultar.setFocusPainted(false);
        btnOcultar.setMargin(new Insets(0, 0, 0, 0));
        btnOcultar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOcultar.addActionListener(e -> {
            ocultarSaldo = !ocultarSaldo;
            atualizarSaldoTela();
        });
        linhaSaldo.add(lblSaldoTitulo, BorderLayout.WEST);
        linhaSaldo.add(btnOcultar, BorderLayout.EAST);
        lblSaldoMenu = esquerda(rot(" ", 34, Font.BOLD, BRANCO));
        saldo.add(linhaSaldo);
        saldo.add(Box.createVerticalStrut(4));
        saldo.add(lblSaldoMenu);
        esq.add(saldo);
        esq.add(espaco(8));
        lblMsgMenu = esquerda(rot(" ", 14, Font.BOLD, VERDE));
        esq.add(lblMsgMenu);
        cab.add(esq, BorderLayout.WEST);

        JPanel dir = new JPanel();
        dir.setOpaque(false);
        dir.setLayout(new BoxLayout(dir, BoxLayout.Y_AXIS));
        JPanel linhaAvatar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linhaAvatar.setOpaque(false);
        linhaAvatar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JPanel avatar = arredondado(AZUL, AZUL, 44);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(44, 44));
        lblAvatar = new JLabel("C");
        lblAvatar.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAvatar.setForeground(BRANCO);
        avatar.add(lblAvatar);
        linhaAvatar.add(avatar);
        dir.add(linhaAvatar);
        dir.add(espaco(10));

        cartaoHome = arredondado(AZUL, AZUL, 24);
        cartaoHome.setLayout(new BorderLayout());
        cartaoHome.setBorder(new EmptyBorder(16, 20, 16, 20));
        cartaoHome.setPreferredSize(new Dimension(320, 160));
        cartaoHome.setMaximumSize(new Dimension(320, 160));
        JLabel marca = new JLabel("SENAI BANK");
        marca.setFont(new Font("SansSerif", Font.BOLD, 15));
        marca.setForeground(BRANCO);
        lblHomeCartao = new JLabel();
        lblHomeCartao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblHomeCartao.setForeground(BRANCO);
        lblHomeNumero = new JLabel();
        lblHomeNumero.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblHomeNumero.setForeground(BRANCO);
        cartaoHome.add(marca, BorderLayout.NORTH);
        cartaoHome.add(lblHomeCartao, BorderLayout.CENTER);
        cartaoHome.add(lblHomeNumero, BorderLayout.SOUTH);
        dir.add(cartaoHome);
        cab.add(dir, BorderLayout.EAST);
        p.add(cab, BorderLayout.NORTH);

        // ----- Parte central: Operações essenciais (4 cartões expandidos) + Movimentações
        JPanel corpo = new JPanel(new BorderLayout(0, 22));
        corpo.setOpaque(false);

        // Bloco de operações
        JPanel blocoOp = new JPanel(new BorderLayout(0, 14));
        blocoOp.setOpaque(false);
        JPanel tituloMeio = new JPanel(new BorderLayout(0, 14));
        tituloMeio.setOpaque(false);
        JSeparator divisoria = new JSeparator();
        divisoria.setForeground(BORDA);
        JLabel titulo = new JLabel("Operações essenciais");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TEXTO);
        tituloMeio.add(divisoria, BorderLayout.NORTH);
        tituloMeio.add(titulo, BorderLayout.CENTER);
        blocoOp.add(tituloMeio, BorderLayout.NORTH);

        // Grade em 1 linha de 4 colunas ou 2x2 ocupando toda a largura
        JPanel grade = new JPanel(new GridLayout(1, 4, 18, 0));
        grade.setOpaque(false);
        grade.setPreferredSize(new Dimension(0, 115));
        grade.add(tile("saldo", "Consultar saldo", "Veja quanto tem agora",
                AZUL, AZUL_CLARO, () -> consultarSaldo()));
        grade.add(tile("deposito", "Depósito", "Adicione dinheiro",
                VERDE, new Color(0xDC, 0xF5, 0xE6), () -> abrirOperacao(false, "")));
        grade.add(tile("saque", "Sacar dinheiro", "Retire valores",
                new Color(0xEA, 0x58, 0x0C), new Color(0xFD, 0xE8, 0xDA), () -> abrirOperacao(true, "")));
        grade.add(tile("encerrar", "Encerrar", "Finalizar sessão",
                VERMELHO, new Color(0xFD, 0xE2, 0xE2), () -> encerrar()));
        blocoOp.add(grade, BorderLayout.CENTER);

        corpo.add(blocoOp, BorderLayout.NORTH);

        // Bloco de movimentações recentes (preenche o restante da tela)
        JPanel blocoMov = new JPanel(new BorderLayout(0, 12));
        blocoMov.setOpaque(false);

        JPanel cabMov = new JPanel(new BorderLayout());
        cabMov.setOpaque(false);
        JLabel tituloMov = new JLabel("Últimas movimentações");
        tituloMov.setFont(new Font("SansSerif", Font.BOLD, 18));
        tituloMov.setForeground(TEXTO);

        JButton btnVerTodas = new JButton("Ver extrato completo ›");
        btnVerTodas.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnVerTodas.setForeground(AZUL);
        btnVerTodas.setContentAreaFilled(false);
        btnVerTodas.setBorderPainted(false);
        btnVerTodas.setFocusPainted(false);
        btnVerTodas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVerTodas.addActionListener(e -> abrirHistorico());

        cabMov.add(tituloMov, BorderLayout.WEST);
        cabMov.add(btnVerTodas, BorderLayout.EAST);
        blocoMov.add(cabMov, BorderLayout.NORTH);

        JPanel cardLista = arredondado(BRANCO, BRANCO, 22);
        cardLista.putClientProperty("borda", BORDA);
        cardLista.putClientProperty("sombra", true);
        cardLista.setLayout(new BorderLayout());
        cardLista.setBorder(new EmptyBorder(16, 20, 20, 20));

        areaMovimentacoesHome = new JTextArea();
        areaMovimentacoesHome.setEditable(false);
        areaMovimentacoesHome.setOpaque(false);
        areaMovimentacoesHome.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaMovimentacoesHome.setForeground(TEXTO);

        JScrollPane scrollMov = new JScrollPane(areaMovimentacoesHome);
        scrollMov.setBorder(null);
        scrollMov.setOpaque(false);
        scrollMov.getViewport().setOpaque(false);
        cardLista.add(scrollMov, BorderLayout.CENTER);

        blocoMov.add(cardLista, BorderLayout.CENTER);
        corpo.add(blocoMov, BorderLayout.CENTER);

        p.add(corpo, BorderLayout.CENTER);
        return p;
    }

    // =============== TELA DE EXTRATO / MOVIMENTAÇÕES COMPLETAS ===============
    static JPanel criarTelaHistorico() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(FUNDO);
        p.setBorder(new EmptyBorder(32, 42, 32, 42));

        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JLabel titulo = new JLabel("Extrato de Movimentações");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(TEXTO);

        JButton voltar = botao("← Voltar ao início", AZUL_CLARO, AZUL);
        voltar.setPreferredSize(new Dimension(180, 42));
        voltar.addActionListener(e -> abrirMenu(""));

        cab.add(titulo, BorderLayout.WEST);
        cab.add(voltar, BorderLayout.EAST);
        p.add(cab, BorderLayout.NORTH);

        JPanel card = arredondado(BRANCO, BRANCO, 22);
        card.putClientProperty("borda", BORDA);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        areaHistoricoCompleto = new JTextArea();
        areaHistoricoCompleto.setEditable(false);
        areaHistoricoCompleto.setOpaque(false);
        areaHistoricoCompleto.setFont(new Font("Monospaced", Font.PLAIN, 15));
        areaHistoricoCompleto.setForeground(TEXTO);

        JScrollPane scroll = new JScrollPane(areaHistoricoCompleto);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    static void abrirHistorico() {
        atualizarListaMovimentacoes();
        mostrar("historico");
    }

    static void atualizarListaMovimentacoes() {
        StringBuilder sbHome = new StringBuilder();
        StringBuilder sbCompleto = new StringBuilder();
        int encontrados = 0;

        for (int i = movimentacoes.size() - 1; i >= 0; i--) {
            String[] partes = movimentacoes.get(i).split(";");
            int cartaoIdx = Integer.parseInt(partes[0]);
            if (cartaoIdx == c) {
                String tipo = partes[1];
                String valor = partes[2];
                String data = partes[3];
                String sinal = tipo.equals("Depósito") ? "+" : "-";

                String linhaFormatada = String.format("%-12s | %-15s | %s\n",
                        tipo.toUpperCase(), sinal + " " + valor, data);

                if (encontrados < 6) {
                    sbHome.append(linhaFormatada).append("\n");
                }
                sbCompleto.append(linhaFormatada)
                        .append("------------------------------------------------------------------\n");
                encontrados++;
            }
        }

        if (encontrados == 0) {
            String vazio = "Nenhuma movimentação registrada neste cartão até o momento.";
            if (areaMovimentacoesHome != null) areaMovimentacoesHome.setText(vazio);
            if (areaHistoricoCompleto != null) areaHistoricoCompleto.setText(vazio);
        } else {
            if (areaMovimentacoesHome != null) areaMovimentacoesHome.setText(sbHome.toString());
            if (areaHistoricoCompleto != null) areaHistoricoCompleto.setText(sbCompleto.toString());
        }
    }

    // Barra lateral: logo, 5 itens de menu, perfil e botão Encerrar
    static JPanel criarSidebar() {
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBackground(BRANCO);
        s.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDA), new EmptyBorder(28, 18, 26, 18)));
        s.setPreferredSize(new Dimension(240, 0));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel quadrado = arredondado(AZUL, AZUL, 10);
        quadrado.setLayout(new GridBagLayout());
        quadrado.setPreferredSize(new Dimension(34, 34));
        JLabel letra = new JLabel("S");
        letra.setFont(new Font("SansSerif", Font.BOLD, 18));
        letra.setForeground(BRANCO);
        quadrado.add(letra);
        JLabel nomeBanco = new JLabel("SENAI BANK");
        nomeBanco.setFont(new Font("SansSerif", Font.BOLD, 16));
        nomeBanco.setForeground(AZUL);
        logo.add(quadrado);
        logo.add(nomeBanco);
        s.add(logo);
        s.add(espaco(28));

        JLabel menu = new JLabel("MENU");
        menu.setFont(new Font("SansSerif", Font.BOLD, 11));
        menu.setForeground(CINZA);
        s.add(linhaEsquerda(menu));
        s.add(espaco(8));

        btnNavInicio = botao("Início", AZUL_CLARO, AZUL);
        btnNavSaldo = botao("Consultar saldo", BRANCO, TEXTO);
        btnNavDeposito = botao("Depósito", BRANCO, TEXTO);
        btnNavSaque = botao("Saque", BRANCO, TEXTO);
        btnNavHist = botao("Movimentações", BRANCO, TEXTO);

        btnNavInicio.addActionListener(e -> abrirMenu(""));
        btnNavSaldo.addActionListener(e -> consultarSaldo());
        btnNavDeposito.addActionListener(e -> abrirOperacao(false, ""));
        btnNavSaque.addActionListener(e -> abrirOperacao(true, ""));
        btnNavHist.addActionListener(e -> abrirHistorico());

        JButton[] itens = {btnNavInicio, btnNavSaldo, btnNavDeposito, btnNavSaque, btnNavHist};
        for (JButton b : itens) {
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(0, 18, 0, 10));
            b.setPreferredSize(new Dimension(200, 46));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            s.add(b);
            s.add(espaco(8));
        }
        s.add(Box.createVerticalGlue());

        lblPerfilNome = new JLabel(" ");
        lblPerfilNome.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPerfilNome.setForeground(TEXTO);
        lblPerfilCartao = new JLabel(" ");
        lblPerfilCartao.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPerfilCartao.setForeground(CINZA);
        s.add(linhaEsquerda(lblPerfilNome));
        s.add(linhaEsquerda(lblPerfilCartao));
        s.add(espaco(12));

        JButton sair = botao("Encerrar", BRANCO, VERMELHO);
        sair.setPreferredSize(new Dimension(200, 46));
        sair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        sair.setAlignmentX(Component.CENTER_ALIGNMENT);
        sair.addActionListener(e -> encerrar());
        s.add(sair);
        return s;
    }

    static void consultarSaldo() {
        JOptionPane.showMessageDialog(null, "Seu saldo é: " + reais(saldos[c]),
                "Saldo", JOptionPane.INFORMATION_MESSAGE);
    }

    static void encerrar() {
        JOptionPane.showMessageDialog(null, usuario + ", atendimento encerrado com sucesso!");
        txtNome.setText("");
        escolherCartao(0);
        mostrar("login");
    }

    static void atualizarSaldoTela() {
        lblSaldoMenu.setText(ocultarSaldo ? "R$ ••••••" : reais(saldos[c]));
        btnOcultar.setText(ocultarSaldo ? "Mostrar" : "Ocultar");
    }

    static void abrirMenu(String mensagem) {
        lblOla.setText("Olá, " + usuario);
        lblSaldoTitulo.setText("Saldo disponível · Cartão " + numeros[c]);
        atualizarSaldoTela();
        atualizarListaMovimentacoes();
        lblMsgMenu.setText(mensagem.isEmpty() ? " " : mensagem);
        lblAvatar.setText(usuario.substring(0, 1).toUpperCase());
        lblHomeCartao.setText(NOME_CARTAO);
        lblHomeNumero.setText("•••• •••• •••• " + numeros[c]);
        lblPerfilNome.setText(usuario);
        lblPerfilCartao.setText("Cartão " + numeros[c]);
        cartaoHome.putClientProperty("cor1", new Color(0x0B, 0x3A, 0x9E));   // azul do cartão SBank
        cartaoHome.putClientProperty("cor2", new Color(0x25, 0x63, 0xEB));
        cartaoHome.repaint();
        mostrar("menu");
    }

    static void mostrar(String nome) {
        if (nome.equals("menu") || nome.equals("valor") || nome.equals("conf")
                || nome.equals("recibo") || nome.equals("historico")) {
            layoutConteudo.show(conteudo, nome);
            layout.show(telas, "app");
            atualizarNav(nome);
        } else {
            layout.show(telas, nome);
        }
    }

    static void atualizarNav(String nome) {
        boolean inicio = nome.equals("menu");
        boolean deposito = nome.equals("valor") && !modoSaque;
        boolean saque = (nome.equals("valor") && modoSaque) || nome.equals("conf") || nome.equals("recibo");
        boolean hist = nome.equals("historico");
        cores(btnNavInicio, inicio ? AZUL_CLARO : BRANCO, inicio ? AZUL : TEXTO);
        cores(btnNavDeposito, deposito ? AZUL_CLARO : BRANCO, deposito ? AZUL : TEXTO);
        cores(btnNavSaque, saque ? AZUL_CLARO : BRANCO, saque ? AZUL : TEXTO);
        cores(btnNavHist, hist ? AZUL_CLARO : BRANCO, hist ? AZUL : TEXTO);
    }

    static JPanel centralizar(JPanel tela, int largura, int altura) {
        tela.setPreferredSize(new Dimension(largura, altura));
        JPanel fora = new JPanel(new GridBagLayout());
        fora.setBackground(FUNDO);
        fora.add(tela);
        return fora;
    }

    static JLabel esquerda(JLabel l) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // =============== TELA 3: SAQUE / DEPÓSITO ===============
    static JPanel criarOperacao() {
        JPanel p = tela();
        p.add(espaco(4));
        p.add(espaco(12));

        JPanel abas = new JPanel(new GridLayout(1, 2, 8, 0));
        abas.setOpaque(false);
        abas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        abas.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTabSaque = botao("Saque", AZUL, BRANCO);
        btnTabDeposito = botao("Depósito", BRANCO, TEXTO);
        btnTabSaque.addActionListener(e -> abrirOperacao(true, ""));
        btnTabDeposito.addActionListener(e -> abrirOperacao(false, ""));
        abas.add(btnTabSaque);
        abas.add(btnTabDeposito);
        p.add(abas);
        p.add(espaco(14));

        lblSaldoOp = rot(" ", 13, Font.PLAIN, CINZA);
        p.add(lblSaldoOp);
        p.add(espaco(8));
        lblTituloValor = rot(" ", 16, Font.BOLD, TEXTO);
        p.add(lblTituloValor);
        p.add(espaco(8));

        txtValor = new JTextField();
        txtValor.setFont(new Font("SansSerif", Font.BOLD, 28));
        txtValor.setHorizontalAlignment(JTextField.CENTER);
        txtValor.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(8, 14, 8, 14)));
        txtValor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        txtValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtValor.addActionListener(e -> confirmar());
        p.add(txtValor);
        p.add(espaco(12));
        p.add(chipsOperacao());

        lblErroOp = rot(" ", 14, Font.BOLD, VERMELHO);
        p.add(espaco(8));
        p.add(lblErroOp);
        p.add(espaco(8));

        btnConfirmar = botao("Confirmar", AZUL, BRANCO);
        btnConfirmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.addActionListener(e -> confirmar());
        p.add(btnConfirmar);
        p.add(espaco(10));

        JButton cancelar = botao("Cancelar operação", BRANCO, TEXTO);
        cancelar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        cancelar.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelar.addActionListener(e -> abrirMenu(""));
        p.add(cancelar);
        return p;
    }

    static JPanel chipsOperacao() {
        JPanel linha = new JPanel(new GridLayout(1, 4, 8, 0));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        linha.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] valores = {"50", "100", "200", "500"};
        for (String v : valores) {
            JButton b = botao(v, AZUL_CLARO, AZUL);
            b.addActionListener(e -> txtValor.setText(v));
            linha.add(b);
        }
        return linha;
    }

    static void abrirOperacao(boolean saque, String valor) {
        modoSaque = saque;
        txtValor.setText(valor);
        lblErroOp.setText(" ");
        cores(btnTabSaque, saque ? AZUL : BRANCO, saque ? BRANCO : TEXTO);
        cores(btnTabDeposito, saque ? BRANCO : AZUL, saque ? TEXTO : BRANCO);
        lblSaldoOp.setText("Saldo disponível: " + reais(saldos[c]));
        lblTituloValor.setText(saque ? "Valor do saque (R$)" : "Valor do depósito (R$)");
        btnConfirmar.setText(saque ? "Confirmar saque" : "Confirmar depósito");
        mostrar("valor");
        txtValor.requestFocusInWindow();
    }

    static void confirmar() {
        double valor = lerValor(txtValor.getText());
        String erro = modoSaque ? sacar(c, valor) : depositar(c, valor);

        if (!erro.isEmpty()) {
            lblErroOp.setText(erro);
            return;
        }
        if (modoSaque) {
            ultimoSaque = valor;
            lblConfSaque.setText(reais(valor));
            lblConfSaldo.setText(reais(saldos[c]));
            mostrar("conf");
        } else {
            abrirMenu("Depósito realizado com sucesso!");
        }
    }

    // =============== TELA 4: SAQUE CONCLUÍDO ===============
    static JPanel criarConfirmacao() {
        JPanel p = tela();
        p.add(espaco(4));
        p.add(espaco(20));
        p.add(circulo("✓", VERDE));
        p.add(espaco(12));
        p.add(rot("Transação concluída!", 20, Font.BOLD, TEXTO));
        p.add(espaco(16));

        JPanel caixa = arredondado(BRANCO, BRANCO, 20);
        caixa.putClientProperty("borda", BORDA);
        caixa.setLayout(new BoxLayout(caixa, BoxLayout.Y_AXIS));
        caixa.setBorder(new EmptyBorder(12, 16, 12, 16));
        caixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        caixa.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblConfSaque = new JLabel();
        lblConfSaldo = new JLabel();
        caixa.add(linha("Saque", lblConfSaque));
        caixa.add(linha("Saldo atual", lblConfSaldo));
        p.add(caixa);
        p.add(espaco(18));

        p.add(rot("Deseja imprimir o comprovante?", 15, Font.PLAIN, CINZA));
        p.add(espaco(12));

        JButton sim = botao("Gerar comprovante (.txt)", AZUL, BRANCO);
        sim.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        sim.setAlignmentX(Component.CENTER_ALIGNMENT);
        sim.addActionListener(e -> pedirComprovante());
        p.add(sim);
        p.add(espaco(10));

        JButton nao = botao("Concluir sem comprovante", BRANCO, TEXTO);
        nao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        nao.setAlignmentX(Component.CENTER_ALIGNMENT);
        nao.addActionListener(e -> abrirMenu("Saque concluído, sem comprovante."));
        p.add(nao);
        return p;
    }

    static void pedirComprovante() {
        try {
            areaRecibo.setText(gravarComprovante(c, ultimoSaque, usuario));
            mostrar("recibo");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao gerar o comprovante.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============== TELA 5: COMPROVANTE ===============
    static JPanel criarRecibo() {
        JPanel p = tela();
        p.add(espaco(4));
        p.add(espaco(20));
        p.add(circulo("✓", VERDE));
        p.add(espaco(12));
        p.add(rot("Comprovante gerado", 20, Font.BOLD, TEXTO));
        p.add(espaco(16));

        areaRecibo = new JTextArea(5, 20);
        areaRecibo.setEditable(false);
        areaRecibo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaRecibo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA, 1, true), new EmptyBorder(12, 14, 12, 14)));
        areaRecibo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        areaRecibo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(areaRecibo);
        p.add(espaco(10));
        p.add(rot("Arquivo salvo: comprovante_senai_bank.txt", 13, Font.PLAIN, CINZA));
        p.add(espaco(18));

        JButton voltar = botao("Voltar ao painel", AZUL, BRANCO);
        voltar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        voltar.setAlignmentX(Component.CENTER_ALIGNMENT);
        voltar.addActionListener(e -> abrirMenu(""));
        p.add(voltar);
        return p;
    }

    // =============== TELA 6: ACESSO NEGADO ===============
    static JPanel criarBloqueio() {
        JPanel p = tela();
        p.add(topo());
        p.add(espaco(40));
        p.add(circulo("!", VERMELHO));
        p.add(espaco(14));
        p.add(rot("Acesso negado", 22, Font.BOLD, TEXTO));
        p.add(espaco(8));
        p.add(rot("Tentativas esgotadas. Atendimento encerrado.", 14, Font.BOLD, VERMELHO));
        p.add(espaco(24));

        JButton reiniciar = botao("Reiniciar", AZUL, BRANCO);
        reiniciar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        reiniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
        reiniciar.addActionListener(e -> {
            txtNome.setText("");
            escolherCartao(0);
            mostrar("login");
        });
        p.add(reiniciar);
        return p;
    }

    // =============== PEÇAS REUTILIZÁVEIS ===============
    static JLabel rot(String texto, int tamanho, int estilo, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", estilo, tamanho));
        l.setForeground(cor);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    static JPanel tela() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(FUNDO);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        return p;
    }

    static Component espaco(int altura) {
        return Box.createRigidArea(new Dimension(0, altura));
    }

    static JLabel topo() {
        return rot("SENAI BANK", 16, Font.BOLD, AZUL);
    }

    static JPanel linha(String nome, JLabel valor) {
        valor.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel n = new JLabel(nome);
        n.setFont(new Font("SansSerif", Font.PLAIN, 14));
        n.setForeground(CINZA);
        JPanel l = new JPanel(new BorderLayout());
        l.setOpaque(false);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        l.add(n, BorderLayout.WEST);
        l.add(valor, BorderLayout.EAST);
        return l;
    }

    static JPanel circulo(String sinal, Color cor) {
        JPanel a = arredondado(cor, cor, 64);
        a.setLayout(new GridBagLayout());
        a.setPreferredSize(new Dimension(64, 64));
        a.setMaximumSize(new Dimension(64, 64));
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel s = new JLabel(sinal);
        s.setFont(new Font("SansSerif", Font.BOLD, 32));
        s.setForeground(BRANCO);
        a.add(s);
        return a;
    }

    static JButton botao(String texto, Color fundo, Color corTexto) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor((Color) getClientProperty("fundo"));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(BORDA);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.putClientProperty("fundo", fundo);
        b.setForeground(corTexto);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static void cores(JButton b, Color fundo, Color corTexto) {
        b.putClientProperty("fundo", fundo);
        b.setForeground(corTexto);
        b.repaint();
    }

    static JPanel arredondado(Color cor1, Color cor2, int arco) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sombra = Boolean.TRUE.equals(getClientProperty("sombra"));
                int w = getWidth(), h = getHeight() - (sombra ? 6 : 0);
                if (sombra) {
                    g2.setColor(new Color(20, 33, 61, 12));
                    g2.fillRoundRect(2, 5, w - 5, h, arco, arco);
                    g2.setColor(new Color(20, 33, 61, 10));
                    g2.fillRoundRect(1, 3, w - 3, h, arco, arco);
                }
                g2.setPaint(new GradientPaint(0, 0, (Color) getClientProperty("cor1"),
                        w, h, (Color) getClientProperty("cor2")));
                g2.fillRoundRect(0, 0, w - 1, h - 1, arco, arco);
                Color borda = (Color) getClientProperty("borda");
                if (borda != null) {
                    g2.setColor(borda);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arco, arco);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.putClientProperty("cor1", cor1);
        p.putClientProperty("cor2", cor2);
        return p;
    }

    static JPanel icone(String tipo, Color cor, Color fundo) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(fundo);
                g2.fillOval(cx - 23, cy - 23, 46, 46);
                g2.setColor(cor);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (tipo.equals("saldo")) {
                    g2.drawRoundRect(cx - 11, cy - 8, 22, 16, 6, 6);
                    g2.fillOval(cx + 4, cy - 2, 4, 4);
                } else if (tipo.equals("deposito")) {
                    g2.drawLine(cx, cy - 9, cx, cy + 7);
                    g2.drawLine(cx - 6, cy + 1, cx, cy + 7);
                    g2.drawLine(cx + 6, cy + 1, cx, cy + 7);
                } else if (tipo.equals("saque")) {
                    g2.drawLine(cx, cy + 9, cx, cy - 7);
                    g2.drawLine(cx - 6, cy - 1, cx, cy - 7);
                    g2.drawLine(cx + 6, cy - 1, cx, cy - 7);
                } else {
                    g2.drawArc(cx - 9, cy - 8, 18, 18, 120, 300);
                    g2.drawLine(cx, cy - 11, cx, cy - 1);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(50, 50));
        return p;
    }

    static JPanel tile(String tipo, String titulo, String subtitulo, Color corIcone, Color fundoIcone, Runnable acao) {
        JPanel t = arredondado(BRANCO, BRANCO, 22);
        t.putClientProperty("sombra", true);
        t.putClientProperty("borda", BORDA);
        t.setLayout(new BorderLayout(14, 0));
        t.setBorder(new EmptyBorder(14, 18, 20, 18));
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(titulo);
        a.setFont(new Font("SansSerif", Font.BOLD, 16));
        a.setForeground(TEXTO);
        JLabel b = new JLabel(subtitulo);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(CINZA);
        textos.add(Box.createVerticalGlue());
        textos.add(a);
        textos.add(b);
        textos.add(Box.createVerticalGlue());
        JLabel seta = new JLabel("›");
        seta.setFont(new Font("SansSerif", Font.PLAIN, 28));
        seta.setForeground(CINZA);

        t.add(icone(tipo, corIcone, fundoIcone), BorderLayout.WEST);
        t.add(textos, BorderLayout.CENTER);
        t.add(seta, BorderLayout.EAST);

        Color hover = new Color(0xF5, 0xF8, 0xFF);
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                acao.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                t.putClientProperty("cor1", hover);
                t.putClientProperty("cor2", hover);
                t.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                t.putClientProperty("cor1", BRANCO);
                t.putClientProperty("cor2", BRANCO);
                t.repaint();
            }
        });
        return t;
    }

    static JPanel linhaEsquerda(JLabel l) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(l);
        return p;
    }
}