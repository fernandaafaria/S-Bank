🏦 S-Bank — Simulador de Terminal de Autoatendimento Bancário"Por trás de uma operação simples do dia a dia existe toda uma sequência lógica de passos."O S-Bank é um simulador de caixa eletrônico desenvolvido em Java (Swing/AWT) que transforma a lógica fundamental de programação em uma experiência visual moderna, segura e intuitiva.🌐 Protótipo Interativo Online (GitHub Pages): Acesse o protótipo navegável do S-Bank(Versão web desenvolvida como protótipo interativo para demonstração rápida em navegadores e dispositivos móveis, sem necessidade de instalação).💡 1. Visão Geral e Origem do ProjetoO projeto surgiu durante as aulas de LOPAL (Lógica de Programação e Algoritmos) a partir de uma provocação simples: como funcionam as coisas que usamos todos os dias?Quando vamos a um caixa eletrônico sacar dinheiro ou conferir o saldo, a operação parece instantânea. Porém, por trás de cada toque na tela existe uma sequência lógica de passos rigorosa — laços de repetição, estruturas condicionais, contagem de tentativas e validação de saldo.Versão 1.0 (Núcleo LOPAL — Console): Desenvolvida 100% no terminal utilizando Scanner, laço do-while e uma variável saldo = 0.0 dentro do método main.Versão 2.0 (S-Bank Atual — Desktop & Protótipo Web): Evolução do mesmo núcleo de regras de negócio para uma interface gráfica moderna em Java Swing, adicionando múltiplos cartões via vetores paralelos, autenticação por PIN de 4 dígitos, histórico de movimentações, emissão de comprovante em .txt e um protótipo web interativo.📋 2. Especificação de Requisitos (LOPAL → S-Bank)2.1 Requisitos Funcionais (RF)CódigoRequisitoOrigemRF01Inicialização de Saldo: O sistema inicia com valores de saldo pré-definidos em memória (evoluindo da variável única 0.0 do terminal para o vetor paralelo saldos[] com R$ 1.000,00 e R$ 200,00).LOPAL → S-BankRF02Menu de Operações Essenciais: Disponibiliza Consultar Saldo, Depósito, Saque, Movimentações (Extrato) e Encerrar (Sair).LOPAL → S-BankRF03Persistência da Sessão: O painel permanece ativo ao final de cada operação, permitindo múltiplas transações seguidas até que o usuário clique em Encerrar.LOPAL → S-BankRF04Leitura e Tratamento de Entradas: Substitui a leitura via Scanner por campos gráficos e teclado numérico virtual, tratando separadores decimais (, ou .) e entradas inválidas.LOPAL → S-BankRF05Consulta de Saldo Formatada: Exibe o saldo no padrão monetário brasileiro (R$ 0,00), incluindo botão de privacidade (Ocultar / Mostrar).LOPAL → S-BankRF06Regra de Depósito: Aceita apenas valores estritamente maiores que zero (valor > 0); caso contrário, exibe "Erro: Valor inválido.".LOPAL → S-BankRF07Regra de Saque: Subtrai o valor apenas se houver saldo suficiente (valor <= saldos[c]) e se o valor for positivo; caso contrário, exibe "Erro: Saldo insuficiente." ou "Erro: Valor inválido.".LOPAL → S-BankRF08Autenticação e Comprovante: Exige PIN de 4 dígitos para acesso ao cartão selecionado e permite exportar o recibo do último saque para o arquivo local comprovante_sbank.txt.Evolução S-Bank2.2 Requisitos Não Funcionais (RNF)RNF01 (Linguagem e Portabilidade): Aplicação principal escrita integralmente em Java (compilável no JDK 8 ou superior), utilizando Java Swing e AWT (Graphics2D / CardLayout).RNF02 (Segurança de Acesso): Bloqueio automático da sessão após 3 tentativas consecutivas de senha incorreta.RNF03 (Arquitetura em Memória): Gerenciamento de contas e extrato em memória (via vetores paralelos e ArrayList), sem dependência de banco de dados externo (SQL/NoSQL), com persistência local opcional em .txt para comprovantes.RNF04 (Distribuição e Prototipação): Disponibilização como executável multiplataforma (.jar) na aba Releases e protótipo navegável em HTML/CSS/JS via GitHub Pages.🏗️ 3. Arquitetura e Fluxo do SistemaTopologia de ExecuçãoAplicação Principal (Desktop): Usuário ↔ JVM (Java Swing) ↔ Sistema de Arquivos Local (comprovante_sbank.txt)Protótipo Interativo (Web): Navegador / Celular ↔ HTTPS ↔ GitHub Pages (index.html)Estrutura de Dados (Vetores Paralelos)O sistema relaciona os dados de cada conta através do índice ativo c (0 ou 1):static String[] numeros = {"0001", "0002"};
static String[] senhas  = {"1234", "0000"};
static double[] saldos  = {1000.0, 200.0};

Fluxograma de Operaçãoflowchart TD
    A([Início: Tela de Login]) --> B[Selecionar Cartão 0001 ou 0002]
    B --> C[Digitar PIN de 4 dígitos]
    C --> D{Senha correta?}
    D -- Não --> E{Tentativa > 3?}
    E -- Não --> C
    E -- Sim --> F([Acesso Negado / Bloqueio])
    D -- Sim --> G[Painel Principal S-Bank]
    G --> H{Escolher Operação}
    H --> I[Consultar / Ocultar Saldo]
    H --> J[Depósito > 0]
    H --> K[Saque <= Saldo]
    H --> L[Ver Extrato]
    K --> M{Gerar Comprovante?}
    M -- Sim --> N[Grava comprovante_sbank.txt]
    M -- Não --> G
    N --> G
    I --> G
    J --> G
    L --> G
    H --> O([Encerrar Sessão])

Diagrama de Classes (Main)classDiagram
    class Main {
        +String[] numeros
        +String NOME_CARTAO
        +String[] senhas
        +double[] saldos
        +ArrayList~String~ movimentacoes
        +int c
        +int tentativa
        +String pin
        +boolean senhaCorreta(int c, String digitada)
        +String depositar(int c, double valor)
        +String sacar(int c, double valor)
        +String gravarComprovante(int c, double saque, String usuario)
        +void main(String[] args)
    }

🔐 4. Credenciais de Teste (Homologação)Para testar o sistema (tanto na versão Java Desktop quanto no Protótipo Web), utilize um dos cartões pré-cadastrados abaixo:CartãoNúmeroSenha (PIN)Saldo InicialCartão S-Bank 100011234R$ 1.000,00Cartão S-Bank 200020000R$ 200,00Dica de Segurança: Experimente errar a senha 3 vezes seguidas propositalmente para testar o fluxo de bloqueio (Acesso negado) do terminal.🚀 5. Como Executar o Projeto e Gerar DocumentaçãoOpção A: Protótipo Interativo Online (Sem Instalação)Acesse diretamente pelo navegador ou celular:
👉 https://fernandaafaria.github.io/teste1/Opção B: Executável Pronto (.jar)Acesse a aba Releases deste repositório.Baixe o arquivo .jar mais recente.Dê dois cliques no arquivo ou execute pelo terminal:java -jar S-Bank.jar

Opção C: Pelo Código-Fonte e Geração de Javadoc (IDE)Clone o repositório:git clone https://github.com/fernandaafaria/teste1.git

Abra o projeto no IntelliJ IDEA (JDK 8 ou superior) e execute a classe org.example.Main.Para gerar a documentação técnica HTML a partir dos comentários Javadoc do código:Acesse Tools → Generate JavaDoc...Defina Visibility level como Package (ou Private).Em Other command line arguments, adicione:-encoding UTF-8 -charset UTF-8 -docencoding UTF-8

🛠️ 6. Solução de Problemas Comuns (Troubleshooting)O arquivo .jar não abre com dois cliques no Windows:
Verifique se o Java (JRE/JDK) está instalado e associado aos arquivos .jar (javaw.exe), ou execute via Prompt de Comando na pasta do arquivo:java -jar S-Bank.jar

Erro ao gerar comprovante_sbank.txt:
Certifique-se de executar o programa em uma pasta onde o usuário tenha permissão de escrita no disco (ex.: Área de Trabalho ou Documentos, evitando pastas protegidas como C:\Program Files).👥 7. Equipe de DesenvolvimentoCarlosFernanda Faria
