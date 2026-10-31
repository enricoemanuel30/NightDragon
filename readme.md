# 🎮 Java Dev Quest - RPG &amp; Quiz Interativo Back-End

**Java Dev Quest** é um jogo desktop estilo RPG/Quiz interativo desenvolvido nativamente em **Java (Swing)** com tema visual inspirado em *IDE Dark Mode*. O jogo foi projetado para reforçar o aprendizado de desenvolvimento Back-End Java, guiando o jogador através de fases teóricas e práticas de código.

---

## 📁 Estrutura de Arquivos Recomendada (VS Code)

Para abrir e editar o projeto no **VS Code**, organize seus arquivos na seguinte estrutura:

```
JavaDevQuest/
├── src/
│   └── JavaDevQuestV3.java
└── README.md

```

---

## 🚀 Como Executar no VS Code

### Pré-requisitos

1. **JDK (Java Development Kit)** versão 8 ou superior instalado.
2. Extensão **Extension Pack for Java** instalada no VS Code.

### Passo a Passo

1. Crie uma pasta no seu computador chamada `JavaDevQuest`.
2. Dentro dela, crie a pasta `src` e coloque o arquivo `JavaDevQuestV3.java` nela.
3. Abra a pasta `JavaDevQuest` no VS Code (`File &gt; Open Folder...`).
4. Abra o arquivo `JavaDevQuestV3.java`.
5. Clique no botão **Run** (acima do método `main`) ou pressione `F5` para compilar e abrir a janela do jogo.

---

## 📚 Módulos e Fases Implementados

Atualmente, o jogo contém **8 Desafios Principais** cobrindo a grade de **Fundamentos e POO**:

### **1\. Sintaxe &amp; Tipos Primitivos**

* **Desafio**: Escolha de tipos inteiros e ponto flutuante de 64-bit (`double`).

### **2\. Orientação a Objetos - Encapsulamento**

* **Desafio**: Proteção de atributos de classe usando modificadores de acesso (`private`) e exposição via `getters` e `setters`.

### **3\. Orientação a Objetos - Herança (extends)**

* **Desafio**: Relação de herança entre classes base e subclasses usando a palavra-chave `extends`.

### **4\. Orientação a Objetos - Reutilização com super**

* **Desafio**: Invocação e encadeamento de construtores da superclasse através do método `super()`.

### **5\. Polimorfismo &amp; Sobrescrita (@Override)**

* **Desafio**: Redefinição de comportamentos em subclasse e validação em tempo de compilação com anotação `@Override`.

### **6\. Abstração &amp; Interfaces**

* **Desafio**: Diferenças fundamentais entre classes abstratas (`abstract class`) e contratos de interface (`interface`).

### **7\. Java Collections Framework**

* **Desafio**: Garantia de unicidade de elementos utilizando a interface `Set` (`HashSet`).

### **8\. Exceções &amp; Resiliência**

* **Desafio**: Garantia de execução e limpeza de recursos com o bloco `finally`.

---

## 🛠️ Recursos Visuais e da Interface (Java Swing)

* **HUD de Status**: Exibe a pontuação de experiência (XP), o cargo atual do desenvolvedor (*Dev Júnior*, *Dev OO Pleno*, *Arquiteto POO*) e a barra de energia/vida do sistema (`JProgressBar`).
* **Terminal Estilizado**: Caixa de exibição de código formatada em HTML com fontes monospace (`Consolas` / `Monospaced`) e tema escuro (*Dark Mode*).
* **Botões Interativos**: Transições de cor em tempo real no clique (Verde para acerto, Vermelho para erro de compilação) e avanço temporizado de fase.
* **Sistema de Game Over / Vitória**: Painel final com relatório de desempenho ao zerar o XP ou concluir todas as fases.

---

## 🔮 Próximos Módulos Planejados

* [ ] **Módulo 3: Arquitetura &amp; Padrões** (Padrão MVC, DTOs e Services)
* [ ] **Módulo 4: Persistência de Dados** (SQL Básica, JDBC e Spring Data JPA)
* [ ] **Módulo 5: APIs RESTful com Spring Boot** (Anotações `@RestController`, `@GetMapping`, `@PostMapping` e Tratamento de Exceções Globais)