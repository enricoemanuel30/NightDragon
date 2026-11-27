# Relatório de Conformidade — Mini Projeto 01: ScreenSaver
**Projeto:** Night Dragon (Beat 'em Up 2D em Java / Swing)  
**Data:** 25 de Novembro de 2026  
**Status Geral:** ✅ Todos os requisitos obrigatórios e desafios adicionais concluídos e validados

---

## 1. Diagnóstico Inicial da Arquitetura

Antes de realizar as adaptações, foi efetuada uma análise estrutural completa do projeto:

- **Classe Principal:** [`main.ThanatosBeatEmUp`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/src/main/ThanatosBeatEmUp.java) (herda de `JFrame`, instancia e exibe o `GamePanel`).
- **Classe da Tela:** [`main.engine.GamePanel`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/src/main/engine/GamePanel.java) (herda de `JPanel`, implementa `Runnable` e executa a Thread de loop a 60 FPS).
- **Ciclo de Atualização:** Método `updateGame()` invocado no loop contínuo da Thread a cada ~16.6ms (`TARGET_TIME = 1000 / 60`).
- **Método de Renderização:** `paintComponent(Graphics g)` em `GamePanel`, com `Graphics2D`, anti-aliasing e Depth Sorting no eixo Y (2.5D).
- **Controles:** `addKeyListener` com `handleKeyPressed` e `handleKeyReleased` gerenciando W, A, S, D / Setas, Espaço (roll), J (soco), K (chute), L (modo fúria) e R (reiniciar).
- **Entidades Existentes:** `Entity` (abstrata), `Player`, `Enemy` (abstrata), `BrutamontesEnemy`, `CeifadorEnemy`, `PistoleiroEnemy`, `Projectile` e `FloatingText`.
- **Colisões Pré-existentes:** AABB (`Rectangle.intersects()`) para combate e tiros.
- **Lacunas Identificadas no Diagnóstico Inicial:**
  1. Ausência de uma coleção de objetos/obstáculos geométricos e moedas (`List<MyShape>` do material).
  2. Ausência de variável inteira de pontuação (`score`).
  3. Ausência de sistema de vidas independentes (`lives`).
  4. Ausência de efeitos sonoros em arquivos `.wav` e de gerenciador de áudio (`SoundManager`).
  5. Ausência de exibição explícita de pontuação e vidas via `g2d.drawString()`.

---

## 2. Matriz de Requisitos do Professor e Status

| Requisito | Status | Arquivo(s) e Método(s) | Explicação Técnica |
| :--- | :---: | :--- | :--- |
| **A. Coleção de Objetos / Obstáculos** | **Concluído** | `GamePanel.java`<br>`WorldShape.java` | Campo `List<WorldShape> worldShapes` inicializado com `new ArrayList<>()` em `initGame()`. |
| **A. Referência ao Personagem** | **Concluído** | `GamePanel.java` (`player`) | Referência para o herói Thanatos Colt (`Player player`). |
| **A. Pontuação inicializada em zero** | **Concluído** | `GamePanel.java` (`initGame()`) | Variável `private int score = 0;` inicializada em zero e resetada a cada nova partida. |
| **B. Personagem com imagem e som** | **Concluído** | `Player.java`<br>`SoundManager.java` | Sprites do herói em alta definição carregados de `res/sprites/player/`, com efeitos sonoros em ações e colisões. |
| **B. Dimensões personalizadas** | **Concluído** | `Player.java`<br>`WorldShape.java` | Dimensões calibradas (Jogador: 42x85; Moeda: 22x22; Caixote: 40x26; Barril: 28x28). |
| **B. Formas geométricas (Círculo, Retângulo, Quadrado)** | **Concluído** | `WorldShape.java`<br>`GamePanel.java` | `CIRCLE_COIN` (círculo amarelo), `RECTANGLE_CRATE` (retângulo de madeira) e `SQUARE_BARREL` (quadrado de reforço). |
| **C. Movimento e Controles** | **Concluído** | `GamePanel.java`<br>`Player.java` | Movimentação fluida nas 4 direções (WASD / Setas), cambalhota (<kbd>Espaço</kbd>) e clamping nos limites da arena (`GROUND_MIN_Y` a `GROUND_MAX_Y`). |
| **C. Atualização no ciclo de execução** | **Concluído** | `GamePanel.java` (`updateGame()`) | Cada objeto e moeda tem seu método `update()` chamado no loop de 60 FPS. |
| **D. Detecção de colisão** | **Concluído** | `GamePanel.java` (`updateGame()`) | Teste AABB `player.getBounds().intersects(shape.getBounds())` entre o personagem e os objetos do mundo. |
| **D. Efeito sonoro em colisão válida** | **Concluído** | `GamePanel.java`<br>`SoundManager.java` | Disparo assíncrono de `playCoin()` (moeda) ou `playHit()` (obstáculo/ataque) sem travar a Thread do jogo. |
| **D. Remoção do objeto atingido** | **Concluído** | `GamePanel.java` (`updateGame()`) | O objeto tem `active = false` para evitar reprocessamento e é removido da lista imediatamente. |
| **D. +10 Pontos por colisão válida** | **Concluído** | `GamePanel.java` (`updateGame()`) | `score += shape.getPoints();` (+10 pontos garantidos a cada colisão). |
| **D. Prevenção de colisões duplicadas** | **Concluído** | `GamePanel.java`<br>`WorldShape.java` | Flag booleana `active = false` desativa a hitbox do objeto no mesmo instante do primeiro impacto. |
| **D. Remoção segura (SEM ConcurrentModificationException)** | **Concluído** | `GamePanel.java` (`updateGame()`) | **Regra estrita atendida:** percorrido por laço decrescente `for (int i = worldShapes.size() - 1; i >= 0; i--)` com `worldShapes.remove(i)`. |
| **E. Desenho dos objetos na tela** | **Concluído** | `GamePanel.java` (`paintComponent`)<br>`WorldShape.java` (`render`) | Chamada `ws.render(g2)` desenhando círculos, retângulos e quadrados com sombras no solo 2.5D. |
| **E. Desenho do personagem** | **Concluído** | `GamePanel.java` (`paintComponent`)<br>`Player.java` (`render`) | Renderização de Thanatos com animações, sombras e Depth Sorting. |
| **E. Exibição da pontuação com g2d.drawString()** | **Concluído** | `GamePanel.java` (`drawHUD`) | `g2.drawString("PONTUAÇÃO: " + score, ...)` no painel superior e `drawOverlayMessage` na tela final. |
| **E. Atualização visual após colisões** | **Concluído** | `GamePanel.java` | Atualização instantânea a 60 FPS no HUD acompanhada de texto flutuante `+10 PTS`. |
| **F. Compilação e Execução** | **Concluído** | `run.bat` / Terminal | Compila com código de saída 0 no JDK 27 e executa sem travar. |

---

## 3. Desafios Adicionais Sugeridos pelo Professor

### Desafio 1: Placar e Vidas
- **Status:** ✅ Concluído.
- **Implementação:** Variável `private int lives = 3;` adicionada em `GamePanel`. Quando o HP de Thanatos chega a zero, 1 vida é descontada e o HP é restaurado para 100 com mensagem flutuante `"VIDA PERDIDA! RESTAM: X"`. Apenas quando as vidas chegam a zero o Game Over é acionado.
- **Exibição:** Desenhado no HUD via `g2.drawString("VIDAS: " + lives + "  |  ONDA: " + currentWave + " / 3", ...)` no canto superior direito.

### Desafio 2: Coletor de Moedas
- **Status:** ✅ Concluído.
- **Implementação:** Moedas de ouro criadas como instâncias de `WorldShape` do tipo `CIRCLE_COIN` (círculos amarelos com gradiente dourado, borda e efeito de flutuação vertical).
- **Detecção e Coleta:** Ao tocar a moeda, ela executa o som `playCoin()`, concede +10 pontos, adiciona +5 de Fúria, exibe notificação `+10 PTS (MOEDA)` e é removida com segurança da coleção.
- **Drop Dinâmico:** Além das moedas espalhadas pelo cenário em cada onda, cada inimigo derrotado dropa uma moeda no local da queda.

---

## 4. Arquivos Criados e Modificados

1. **[`src/Entities/WorldShape.java`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/src/Entities/WorldShape.java) (Criado):**
   - Classe que encapsula as formas geométricas solicitadas (círculos, retângulos e quadrados), suas hitboxes (`Rectangle`), métodos de desenho AWT e ciclo de atualização.
2. **[`src/main/engine/SoundManager.java`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/src/main/engine/SoundManager.java) (Criado):**
   - Gerenciador de áudio baseado na `javax.sound.sampled` API do Java standard.
   - Carrega arquivos `.wav` do disco ou classpath e executa áudios (`hit`, `coin`, `rage`, `roll`) de forma desacoplada em threads secundárias.
3. **[`src/main/engine/GamePanel.java`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/src/main/engine/GamePanel.java) (Modificado):**
   - Adicionada coleção `worldShapes`, variáveis `score` e `lives`.
   - Adicionado spawn de moedas e caixas em `spawnWave()`.
   - Adicionado laço reverso de colisão segura e pontuação em `updateGame()`.
   - Adicionada chamada `ws.render(g2)` em `paintComponent()`.
   - Atualizado o HUD com `g2.drawString()` para pontuação e vidas.
   - Adicionados efeitos sonoros em esquiva, modo fúria e impactos.
4. **[`res/audio/`](file:///C:/Users/enrico.batista/Downloads/NightDragon/NightDragon/res/audio/) (Recursos gerados):**
   - `hit.wav`: som de impacto de soco/colisão.
   - `coin.wav`: som em sino de 2 tons para moedas.
   - `rage.wav`: som de erupção de plasma do modo fúria.
   - `roll.wav`: som de deslize da cambalhota.

---

## 5. Diferenças entre o Exemplo Teórico do Professor e o Jogo Real

| Aspecto | Exemplo do Material (ScreenSaver) | Implementação Real no Night Dragon |
| :--- | :--- | :--- |
| **Nomes das Classes** | `MyCharacter`, `MyShape`, `ScreenSaver` | `Player`, `WorldShape`, `GamePanel` + `ThanatosBeatEmUp` |
| **Perspectiva de Jogo** | 2D plano genérico | 2.5D Beat 'em Up com profundidade no eixo Y e pista delimitada |
| **Remoção Segura** | Exemplo recomendava laço por índice decrescente ou Iterator | Implementado com laço decrescente `for (int i = size - 1; i >= 0; i--)` com `remove(i)` |
| **Formas Geométricas** | Formas abstratas na tela | Contextualizadas tematicamente: Círculos são Moedas/Fichas de Saloon; Retângulos/Quadrados são Caixotes e Barris |
| **Sistema de Som** | Exemplo com `AudioClip` ou arquivo estático | Implementado com `SoundManager` nativo em `javax.sound.sampled.Clip` com suporte assíncrono |

---

## 6. Testes Realizados e Resultados

1. **Compilação Geral do Projeto:**
   - Comando: `javac -encoding UTF-8 -d bin @sources.txt`
   - Resultado: **0 erros e 0 avisos**.
2. **Ciclo de Atualização e Framerate:**
   - Execução contínua a 60 FPS estáveis sem atrasos ou quedas no temporizador.
3. **Teste de Colisão com Remoção Segura:**
   - O jogador colidiu repetidas vezes com moedas e caixas; nenhum `ConcurrentModificationException` foi lançado.
4. **Teste de Pontuação (+10 pontos):**
   - O placar incrementa exatamente +10 pontos por objeto/moeda colidido e exibe o número no HUD.
5. **Teste de Áudio:**
   - Coleta de moeda dispara o som `coin.wav`; impactos disparam `hit.wav`; esquiva dispara `roll.wav`; ativação da fúria dispara `rage.wav`.
6. **Preservação das Funcionalidades Originais:**
   - Socos, chutes, combos, modo fúria, charuto, IA dos inimigos (Pistoleiro, Ceifador, Brutamontes) continuam funcionando com total fidelidade.
