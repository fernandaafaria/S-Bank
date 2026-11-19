# 🏦 S-Bank — Simulador de Terminal de Autoatendimento Bancário

> *"Por trás de uma operação simples do dia a dia existe toda uma sequência lógica de passos."*

O **S-Bank** é um simulador de caixa eletrônico desenvolvido em **Java (Swing/AWT)** que transforma a lógica fundamental de programação em uma experiência visual moderna, segura e intuitiva.

🌐 **Protótipo Interativo Online (GitHub Pages):** [Acesse o protótipo navegável do S-Bank](https://fernandaafaria.github.io/teste1/)  
*(Versão web desenvolvida como protótipo interativo para demonstração rápida em navegadores e dispositivos móveis, sem necessidade de instalação).*

---

## 💡 1. Visão Geral e Origem do Projeto

O projeto surgiu durante as aulas de **LOPAL (Lógica de Programação e Algoritmos)** a partir de uma provocação simples: **como funcionam as coisas que usamos todos os dias?**

Quando vamos a um caixa eletrônico sacar dinheiro ou conferir o saldo, a operação parece instantânea. Porém, por trás de cada toque na tela existe uma sequência lógica de passos rigorosa — laços de repetição, estruturas condicionais, contagem de tentativas e validação de saldo.

* **Versão 1.0 (Núcleo LOPAL — Console):** Desenvolvida 100% no terminal utilizando `Scanner`, laço `do-while` e uma variável `saldo = 0.0` dentro do método `main`.
* **Versão 2.0 (S-Bank Atual — Desktop & Protótipo Web):** Evolução do mesmo núcleo de regras de negócio para uma interface gráfica moderna em Java Swing, adicionando múltiplos cartões via vetores paralelos, autenticação por PIN de 4 dígitos, histórico de movimentações, emissão de comprovante em `.txt` e um protótipo web interativo.

---

## 📋 2. Especificação de Requisitos (LOPAL → S-Bank)

### 2.1 Requisitos Funcionais (RF)

| Código | Requisito | Origem |
| :--- | :--- | :---: |
| **RF01** | **Inicialização de Saldo:** O sistema inicia com valores de saldo pré-definidos em memória (evoluindo da variável única `0.0` do terminal para o vetor paralelo `saldos[]` com `R$ 1.000,00` e `R$ 200,00`). | LOPAL → S-Bank |
| **RF02** | **Menu de Operações Essenciais:** Disponibiliza **Consultar Saldo**, **Depósito**, **Saque**, **Movimentações (Extrato)** e **Encerrar (Sair)**. | LOPAL → S-Bank |
| **RF03** | **Persistência da Sessão:** O painel permanece ativo ao final de cada operação, permitindo múltiplas transações seguidas até que o usuário clique em **Encerrar**. | LOPAL → S-Bank |
| **RF04** | **Leitura e Tratamento de Entradas:** Substitui a leitura via `Scanner` por campos gráficos e teclado numérico virtual, tratando separadores decimais (`,` ou `.`) e entradas inválidas. | LOPAL → S-Bank |
| **RF05** | **Consulta de Saldo Formatada:** Exibe o saldo no padrão monetário brasileiro (`R$ 0,00`), incluindo botão de privacidade (**Ocultar / Mostrar**). | LOPAL → S-Bank |
| **RF06** | **Regra de Depósito:** Aceita apenas valores estritamente maiores que zero (`valor > 0`); caso contrário, exibe `"Erro: Valor inválido."`. | LOPAL → S-Bank |
| **RF07** | **Regra de Saque:** Subtrai o valor apenas se houver saldo suficiente (`valor <= saldos[c]`) e se o valor for positivo; caso contrário, exibe `"Erro: Saldo insuficiente."` ou `"Erro: Valor inválido."`. | LOPAL → S-Bank |
| **RF08** | **Autenticação e Comprovante:** Exige PIN de 4 dígitos para acesso ao cartão selecionado e permite exportar o recibo do último saque para o arquivo local `comprovante_sbank.txt`. | Evolução S-Bank |

### 2.2 Requisitos Não Funcionais (RNF)

* **RNF01 (Linguagem e Portabilidade):** Aplicação principal escrita integralmente em **Java** (compilável no JDK 8 ou superior), utilizando **Java Swing** e **AWT (`Graphics2D` / `CardLayout`)**.
* **RNF02 (Segurança de Acesso):** Bloqueio automático da sessão após **3 tentativas** consecutivas de senha incorreta.
* **RNF03 (Arquitetura em Memória):** Gerenciamento de contas e extrato em memória (via vetores paralelos e `ArrayList`), sem dependência de banco de dados externo (SQL/NoSQL), com persistência local opcional em `.txt` para comprovantes.
* **RNF04 (Distribuição e Prototipação):** Disponibilização como executável multiplataforma (`.jar`) na aba Releases e protótipo navegável em HTML/CSS/JS via GitHub Pages.

---

## 🏗️ 3. Arquitetura e Fluxo do Sistema

### Topologia de Execução
* **Aplicação Principal (Desktop):** `Usuário` ↔ `JVM (Java Swing)` ↔ `Sistema de Arquivos Local (comprovante_sbank.txt)`
* **Protótipo Interativo (Web):** `Navegador / Celular` ↔ `HTTPS` ↔ `GitHub Pages (index.html)`

### Estrutura de Dados (Vetores Paralelos)
O sistema relaciona os dados de cada conta através do índice ativo `c` (`0` ou `1`):

```java
static String[] numeros = {"0001", "0002"};
static String[] senhas  = {"1234", "0000"};
static double[] saldos  = {1000.0, 200.0};

7. Equipe de Desenvolvimento
- Carlos
- Fernanda Faria
- Julia
