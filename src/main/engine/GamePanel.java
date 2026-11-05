package main.engine;

// Importa classes gráficas do AWT para desenho de formas, cores, fontes, gradientes e anti-aliasing
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
// Importa manipuladores de eventos de teclado (KeyAdapter e KeyEvent)
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
// Importa coleções para armazenar e iterar com segurança sobre listas de objetos
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
// Importa o componente base de tela Swing (JPanel)
import javax.swing.*;
// Importa todas as entidades do jogo (Player, Enemy, Brutamontes, Ceifador, Pistoleiro)
import Entities.*;

/**
 * ==''==========================================================================
 * MOTOR PRINCIPAL DO JOGO: GAMEPANEL (GAME LOOP, FÍSICA E RENDERIZAÇÃO)
 * ============================================================================
 * Herda de JPanel (para ser o painel onde a tela é desenhada) e
 * implementa Runnable (para executar o Game Loop em uma Thread dedicada a 60 FPS).
 * 
 * Principais responsabilidades:
 * 1. Manter a taxa de quadros estável (Loop de 60 quadros por segundo).
 * 2. Capturar comandos do teclado do usuário.
 * 3. Atualizar a física, colisões, IA dos inimigos e progressão de fases/ondas.
 * 4. Renderizar a cena utilizando Depth Sorting (ordenação por profundidade Y),
 *    dando a autêntica sensação tridimensional de um jogo Beat 'em Up clássico.
 * ============================================================================
 */
public class GamePanel extends JPanel implements Runnable {

    // ------------------------------------------------------------------------
    // CONSTANTES DA ARENA DE JOGO (ESPAÇO 2.5D NO SALOON)
    // ------------------------------------------------------------------------
    public static final int WIDTH = 900;        // Largura total da tela em pixels
    public static final int HEIGHT = 500;       // Altura total da tela em pixels
    public static final int GROUND_MIN_Y = 310; // Limite superior da pista caminhável (abaixo do balcão e mesas do fundo)
    public static final int GROUND_MAX_Y = 475; // Limite inferior da pista caminhável (frente da tela)

    // ------------------------------------------------------------------------
    // CONTROLE DO GAME LOOP E THREAD (TARGET 60 FPS)
    // ------------------------------------------------------------------------
    private Thread gameThread;                  // Thread dedicada para rodar o loop contínuo do jogo
    private boolean running = false;            // Flag booleana de controle da execução da Thread
    private final int FPS = 60;                 // Taxa alvo de quadros por segundo
    private final long TARGET_TIME = 1000 / FPS;// Tempo alvo por quadro em milissegundos (~16.6 ms por frame)

    // ------------------------------------------------------------------------
    // COLEÇÕES E ENTIDADES ATIVAS NO MUNDO DO JOGO
    // ------------------------------------------------------------------------
    private Player player;                      // Instância do herói Thanatos Colt
    private List<Enemy> enemies;                // Lista dinâmica contendo todos os inimigos vivos da onda atual
    private List<Projectile> projectiles;       // Lista contendo os projéteis disparados em voo
    private List<FloatingText> floatingTexts;   // Lista contendo os números de dano e notificações flutuantes

    // ------------------------------------------------------------------------
    // MÁQUINA DE ESTADOS DO JOGO
    // ------------------------------------------------------------------------
    private int currentWave = 1;                // Número da onda de inimigos atual (1 a 3)
    private boolean gameOver = false;           // Flag ativada quando a vida de Thanatos chega a zero
    private boolean victory = false;            // Flag ativada quando todas as ondas são derrotadas

    // ------------------------------------------------------------------------
    // FLAGS DE ENTRADA DO TECLADO (MOVIMENTAÇÃO FLUIDA)
    // ------------------------------------------------------------------------
    // Usamos booleanos para permitir movimentação diagonal contínua e suave
    private boolean keyUp, keyDown, keyLeft, keyRight;

    // ------------------------------------------------------------------------
    // TEXTURA DE FUNDO / CENÁRIO
    // ------------------------------------------------------------------------
    private BufferedImage backgroundImage;

    /**
     * Construtor do GamePanel.
     * Configura dimensões da janela, foco de teclado, estado inicial e escuta de eventos.
     */
    public GamePanel() {
        // Define o tamanho preferido para que a janela JFrame se adapte exatamente a 900x500 via pack()
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        // Define a cor de fundo padrão escura
        setBackground(new Color(20, 20, 30));
        
        // Torna o painel receptivo a eventos de foco de teclado
        setFocusable(true);

        // Carrega a textura do cenário caso exista arquivo de imagem dedicado
        loadBackground();

        // Inicializa todas as variáveis do jogo e inicia a primeira onda
        initGame();

        // Registra o ouvinte de eventos de teclado (KeyAdapter)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Chamado no momento exato em que qualquer tecla é pressionada
                handleKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Chamado no momento em que uma tecla é solta pelo jogador
                handleKeyReleased(e.getKeyCode());
            }
        });
    }

    /**
     * Inicializa ou reinicia o estado completo do jogo.
     * Recria o jogador, limpa as listas de inimigos e projetais e carrega a onda 1.
     */
    private void initGame() {
        player = new Player(120, 390); // Spawna Thanatos do lado esquerdo do Saloon
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();
        floatingTexts = new ArrayList<>();
        gameOver = false;
        victory = false;
        currentWave = 1;

        // Spawna os inimigos correspondentes à primeira onda
        spawnWave(currentWave);
    }

    /**
     * Spawna os inimigos de acordo com o número da onda atual.
     * Cria uma progressão gradativa de dificuldade com diferentes combinações de inimigos.
     * 
     * @param wave Número da onda (1, 2 ou 3)
     */
    private void spawnWave(int wave) {
        enemies.clear();     // Remove qualquer inimigo remanescente
        projectiles.clear(); // Remove projéteis da onda anterior
        
        // Onda 1: Apresentação básica de combate (1 Pistoleiro e 1 Ceifador no piso do Saloon)
        if (wave == 1) {
            enemies.add(new PistoleiroEnemy(650, 350));
            enemies.add(new CeifadorEnemy(750, 420));
        } 
        // Onda 2: Aumento da pressão com introdução do Brutamontes Tanque
        else if (wave == 2) {
            enemies.add(new PistoleiroEnemy(700, 340));
            enemies.add(new BrutamontesEnemy(750, 390));
            enemies.add(new CeifadorEnemy(800, 450));
        } 
        // Onda 3: Desafio Final / Boss Wave com 4 inimigos combinados
        else if (wave >= 3) {
            enemies.add(new BrutamontesEnemy(650, 350));
            enemies.add(new PistoleiroEnemy(750, 410));
            enemies.add(new CeifadorEnemy(800, 360));
            enemies.add(new BrutamontesEnemy(850, 450));
        }
    }

    /**
     * Método do ciclo de vida do Swing (chamado automaticamente quando o componente é exibido).
     * Garante que a Thread do Game Loop seja criada e iniciada apenas uma única vez com segurança.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null) {
            running = true;
            gameThread = new Thread(this); // Instancia a Thread passando este painel como alvo de execução
            gameThread.start();            // Dispara a execução do método run()
        }
    }

    /**
     * IMPLEMENTAÇÃO DO GAME LOOP CONTÍNUO (60 FPS)
     * Roda indefinidamente enquanto 'running' for verdadeiro.
     * A cada ciclo:
     * 1. Mede o tempo de início do frame.
     * 2. Executa a lógica de física e colisões (updateGame).
     * 3. Solicita a pintura na tela (repaint).
     * 4. Calcula o tempo decorrido e coloca a Thread para dormir (Thread.sleep)
     *    o tempo exato necessário para manter 60 frames estáveis por segundo.
     */
    @Override
    public void run() {
        long start, elapsed, wait;

        while (running) {
            start = System.currentTimeMillis(); // Marca o tempo de início do ciclo atual

            // Atualiza o estado da simulação (física, IA, colisões)
            updateGame();

            // Solicita ao sistema gráfico que chame paintComponent() para desenhar a cena
            repaint();

            // Mede quanto tempo a lógica e renderização levaram para rodar
            elapsed = System.currentTimeMillis() - start;

            // Calcula quantos milissegundos restam para completar a janela de ~16.6ms
            wait = TARGET_TIME - elapsed;

            // Se o processamento atrasou, aplica uma pausa mínima de 5ms para não sobrecarregar a CPU
            if (wait < 0) wait = 5;

            try {
                // Pausa a Thread pelo tempo calculado
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------------------
    // ATUALIZAÇÃO DA LÓGICA DO JOGO (FÍSICA, IA E COLISÕES)
    // ------------------------------------------------------------------------
    /**
     * Executa toda a lógica do jogo a cada quadro:
     * - Movimento de personagens
     * - Testes de colisão entre ataques e corpos (Hitbox vs Hurtbox)
     * - Gerenciamento de vida, fúria e ondas
     */
    private void updateGame() {
        // Se a partida acabou (derrota ou vitória), congela a simulação
        if (gameOver || victory) return;

        // --------------------------------------------------------------------
        // 1. Processar Entrada de Movimentação do Jogador
        // --------------------------------------------------------------------
        int dx = 0, dy = 0;
        if (keyUp) dy -= 1;     // W ou Seta Cima: desloca para o fundo da pista
        if (keyDown) dy += 1;   // S ou Seta Baixo: desloca para a frente da pista
        if (keyLeft) dx -= 1;   // A ou Seta Esquerda: recua
        if (keyRight) dx += 1;  // D ou Seta Direita: avança
        
        // Aplica o movimento resultante a Thanatos
        player.move(dx, dy);

        // --------------------------------------------------------------------
        // 2. Atualizar Estado Interno do Jogador (Timers de ataque e Fúria)
        // --------------------------------------------------------------------
        player.update();

        // --------------------------------------------------------------------
        // 3. Atualizar e Colidir Projéteis (Balas do Pistoleiro)
        // --------------------------------------------------------------------
        // Usa Iterator para permitir remoção segura de projéteis sem lançar ConcurrentModificationException
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update(); // Move a bala horizontalmente

            // Se a bala saiu dos limites da tela, descarta-a
            if (!p.isActive()) {
                projIt.remove();
                continue;
            }

            // Teste de Colisão AABB: Projétil colidindo com a área do corpo do Jogador
            if (p.getBounds().intersects(player.getBounds())) {
                // Aplica dano ao jogador
                player.takeDamage(p.getDamage());
                
                // Exibe o número de dano vermelho subindo sobre o jogador
                floatingTexts.add(new FloatingText("-" + p.getDamage(), player.getX(), player.getY() - 10, Color.RED));
                
                // Desativa e remove o projétil que acertou
                p.setActive(false);
                projIt.remove();

                // Verifica se Thanatos sucumbiu ao tiro
                if (player.getHp() <= 0) {
                    gameOver = true;
                }
            }
        }

        // --------------------------------------------------------------------
        // 4. Atualizar Inimigos, Inteligência Artificial e Combates Corpo a Corpo
        // --------------------------------------------------------------------
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();

            // Se o inimigo foi derrotado nesta iteração
            if (!enemy.isAlive()) {
                // Recompensa o jogador com 25 pontos de Fúria do Dragão
                player.addFuria(25);
                
                // Exibe notificação de ganho de fúria
                floatingTexts.add(new FloatingText("+25 FÚRIA", enemy.getX(), enemy.getY() - 20, Color.YELLOW));
                
                // Remove o inimigo abatido da lista ativa
                enemyIt.remove();
                continue;
            }

            // Executa a tomada de decisão da IA (perseguir, esquivar ou atacar)
            enemy.updateAI(player, projectiles);

            // Atualiza os contadores internos de tempo do inimigo
            enemy.update();

            // ----------------------------------------------------------------
            // 4.1 Colisão de Ataque do Inimigo contra o Jogador
            // ----------------------------------------------------------------
            if (enemy.isAttacking() && enemy.hasHitboxActive() && enemy.getAttackBounds().intersects(player.getBounds())) {
                // Se Thanatos estiver sob o efeito da Fúria do Dragão, ele fica invulnerável a golpes
                if (!player.isInvulnerable()) {
                    player.takeDamage(enemy.getAttackDamage());
                    
                    // Exibe número de dano recebido
                    floatingTexts.add(new FloatingText("-" + enemy.getAttackDamage(), player.getX(), player.getY() - 10, Color.RED));
                    
                    // Desativa a hitbox deste ataque para não causar dano duplicado no mesmo golpe
                    enemy.setHitboxActive(false);

                    // Checa derrota
                    if (player.getHp() <= 0) {
                        gameOver = true;
                    }
                }
            }

            // ----------------------------------------------------------------
            // 4.2 Colisão do Golpe de Thanatos Colt contra o Inimigo
            // ----------------------------------------------------------------
            if (player.isAttacking() && player.hasHitboxActive()) {
                // Se a caixa de ataque do golpe de Thanatos interceptar o corpo deste inimigo
                if (player.getAttackBounds().intersects(enemy.getBounds())) {
                    int dmg = player.getAttackDamage();
                    enemy.takeDamage(dmg);       // Aplica dano ao inimigo
                    
                    if (player.isFuriaActive()) {
                        // MECÂNICA DE LIFESTEAL: Cura HP a cada golpe bem-sucedido na Fúria
                        int healAmount = 8;
                        player.heal(healAmount);
                        floatingTexts.add(new FloatingText("+" + healAmount + " HP", player.getX() - 10, player.getY() - 40, Color.GREEN));
                    } else {
                        player.addFuria(10);     // Fora da fúria, cada acerto premia +10 de Fúria
                    }
                    
                    // Exibe o número de dano infligido (Vermelho flamejante no Rage Mode, Laranja no normal)
                    Color dmgColor = player.isFuriaActive() ? new Color(255, 60, 60) : Color.ORANGE;
                    floatingTexts.add(new FloatingText("-" + dmg, enemy.getX(), enemy.getY() - 15, dmgColor));
                }
            }
        }

        // Desativa a hitbox de Thanatos após testar contra todos os inimigos no frame atual
        if (player.isAttacking() && player.hasHitboxActive()) {
            player.setHitboxActive(false);
        }

        // --------------------------------------------------------------------
        // 5. Atualizar Textos Flutuantes (Animação de subida e expiração)
        // --------------------------------------------------------------------
        Iterator<FloatingText> ftIt = floatingTexts.iterator();
        while (ftIt.hasNext()) {
            FloatingText ft = ftIt.next();
            ft.update(); // Move o texto para cima e reduz o tempo de vida
            if (!ft.isActive()) {
                ftIt.remove(); // Remove textos cujo tempo de vida acabou
            }
        }

        // --------------------------------------------------------------------
        // 6. Checar Conclusão e Progressão de Ondas (Waves)
        // --------------------------------------------------------------------
        // Se todos os inimigos da onda atual foram eliminados
        if (enemies.isEmpty()) {
            currentWave++; // Avança para a próxima onda
            
            // Se concluiu a onda 3, declara vitória total
            if (currentWave > 3) {
                victory = true;
            } else {
                // Spawna a nova onda e avisa na tela
                spawnWave(currentWave);
                floatingTexts.add(new FloatingText("ONDA " + currentWave + "!", WIDTH / 2 - 50, 150, Color.CYAN));
            }
        }
    }

    // ------------------------------------------------------------------------
    // RENDERIZAÇÃO GRÁFICA DA CENA (PIPELINE VISUAL)
    // ------------------------------------------------------------------------
    /**
     * Sobrescreve paintComponent para desenhar todos os elementos da cena:
     * Cenário -> Entidades com Depth Sorting (2.5D) -> Projéteis -> Textos -> HUD -> Overlays
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Habilita Anti-Aliasing para que formas geométricas, linhas e bordas fiquem suaves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // A. Desenha o fundo da arena (Céu escuro cyberpunk, prédios iluminados e pista de poeira)
        drawBackground(g2);

        // B. DEPTH SORTING (ORDENAÇÃO POR PROFUNDIDADE NO EIXO Y)
        // Em Beat 'em Ups 2.5D clássicos, personagens mais ao fundo (menor coordenada Y)
        // devem ser desenhados primeiro. Personagens mais à frente (maior Y) são desenhados por cima.
        List<Entity> renderList = new ArrayList<>();
        renderList.add(player);
        renderList.addAll(enemies);
        
        // Ordena a lista comparando a posição vertical Y dos pés de cada entidade
        renderList.sort((e1, e2) -> Integer.compare(e1.getY(), e2.getY()));

        // Desenha todas as entidades já perfeitamente sobrepostas pela profundidade
        for (Entity e : renderList) {
            e.render(g2);
        }

        // C. Desenha os Projéteis em voo
        for (Projectile p : projectiles) {
            p.render(g2);
        }

        // D. Desenha os Números e Textos de Dano Flutuantes
        for (FloatingText ft : floatingTexts) {
            ft.render(g2);
        }

        // E. Desenha a Interface do Usuário (HUD: Barra de Vida, Fúria, Onda, etc.)
        drawHUD(g2);

        // F. Desenha Telas de Fim de Jogo quando aplicável
        if (gameOver) {
            drawOverlayMessage(g2, "GAME OVER", "Thanatos foi derrotado pelos Devil Cowboys!", Color.RED);
        } else if (victory) {
            drawOverlayMessage(g2, "VITÓRIA DA NOITE!", "Você eliminou os Devil Cowboys da Cidade do Milênio!", Color.GREEN);
        }
    }

    /**
     * Carrega a imagem do cenário se houver um arquivo presente em disco ou recursos.
     */
    private void loadBackground() {
        String[] possiblePaths = {
            "res/backgrounds/stage1.png",
            "res/backgrounds/cenario.png",
            "res/backgrounds/background.png",
            "src/res/backgrounds/stage1.png"
        };
        for (String p : possiblePaths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    backgroundImage = ImageIO.read(f);
                    System.out.println("[GamePanel] Textura de cenário carregada com sucesso a partir de: " + p);
                    return;
                } catch (Exception ignored) {}
            }
        }
        try {
            InputStream is = getClass().getResourceAsStream("/res/backgrounds/stage1.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
                System.out.println("[GamePanel] Textura de cenário carregada do classpath.");
            }
        } catch (Exception ignored) {}
    }

    /**
     * Renderiza o plano de fundo temático Velho Oeste Cibernético / Neo-Noir.
     * Se uma imagem de cenário estiver carregada, desenha a textura.
     * Caso contrário, recorre à renderização procedural com gradientes e néons.
     */
    private void drawBackground(Graphics2D g2) {
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
            return;
        }
        drawProceduralBackground(g2);
    }

    /**
     * Renderização procedural de fundo (Fallback padrão).
     */
    private void drawProceduralBackground(Graphics2D g2) {
        // 1. Céu Noturno com Gradiente (do roxo bem escuro ao violeta da cidade)
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(15, 10, 30), 0, GROUND_MIN_Y, new Color(40, 20, 60));
        g2.setPaint(skyGradient);
        g2.fillRect(0, 0, WIDTH, GROUND_MIN_Y);

        // 2. Silhuetas de Arranha-Céus ao Longe
        g2.setColor(new Color(25, 20, 40));
        g2.fillRect(50, 60, 100, GROUND_MIN_Y - 60);
        g2.fillRect(200, 40, 140, GROUND_MIN_Y - 40);
        g2.fillRect(400, 80, 120, GROUND_MIN_Y - 80);
        g2.fillRect(600, 30, 160, GROUND_MIN_Y - 30);

        // 3. Janelas Iluminadas por Néon Rosa e Azul
        g2.setColor(new Color(255, 0, 128, 180)); // Rosa Neon
        g2.fillRect(220, 70, 15, 25);
        g2.fillRect(250, 70, 15, 25);
        g2.setColor(new Color(0, 230, 255, 180)); // Azul Ciano Neon
        g2.fillRect(630, 60, 20, 20);
        g2.fillRect(680, 60, 20, 20);

        // 4. Pista de Luta (Chão de Poeira Mecânica e Asfalto com gradiente de profundidade)
        GradientPaint groundGradient = new GradientPaint(0, GROUND_MIN_Y, new Color(60, 45, 40), 0, HEIGHT, new Color(25, 20, 20));
        g2.setPaint(groundGradient);
        g2.fillRect(0, GROUND_MIN_Y, WIDTH, HEIGHT - GROUND_MIN_Y);

        // 5. Linhas de Perspectiva delimitando a área caminhável da pista
        g2.setColor(new Color(100, 70, 50, 100));
        g2.drawLine(0, GROUND_MIN_Y, WIDTH, GROUND_MIN_Y); // Linha do horizonte do chão
        g2.drawLine(0, GROUND_MAX_Y, WIDTH, GROUND_MAX_Y); // Linha do limite frontal
    }

    /**
     * Renderiza o Heads-Up Display (HUD):
     * - Painel com retrato de Thanatos, barra de HP e o Charuto de Fúria
     * - Painel superior direito com contador de ondas e contagem de inimigos
     * - Guia de comandos no rodapé da janela
     */
    private void drawHUD(Graphics2D g2) {
        // --------------------------------------------------------------------
        // Painel do Jogador (Canto Superior Esquerdo)
        // --------------------------------------------------------------------
        int panelX = 15;
        int panelY = 15;
        int panelW = 390;
        int panelH = 88;

        // Placa de fundo translúcida com cantos arredondados
        g2.setColor(new Color(10, 10, 20, 230));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
        
        // Borda dourada (ou avermelhada ardente no Modo Fúria)
        if (player.isFuriaActive()) {
            g2.setColor(new Color(255, 60, 40));
        } else {
            g2.setColor(new Color(180, 140, 60));
        }
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 15, 15);

        // Retrato do Thanatos se disponível
        BufferedImage portrait = player.getRagePortrait();
        int contentOffsetX = 25;
        if (portrait != null) {
            // Desenha miniatura do rosto do Thanatos no HUD
            int portSize = 58;
            int portX = panelX + 12;
            int portY = panelY + 15;

            // Fundo escuro do avatar com borda
            g2.setColor(new Color(25, 15, 20));
            g2.fillRoundRect(portX - 2, portY - 2, portSize + 4, portSize + 4, 8, 8);
            g2.drawImage(portrait, portX, portY, portSize, portSize, null);

            g2.setColor(player.isFuriaActive() ? new Color(255, 60, 40) : new Color(150, 110, 50));
            g2.drawRoundRect(portX - 2, portY - 2, portSize + 4, portSize + 4, 8, 8);

            contentOffsetX = portX + portSize + 15;
        }

        // Nome de Thanatos Colt
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (player.isFuriaActive()) {
            g2.setColor(new Color(255, 100, 80));
            g2.drawString("THANATOS COLT [MODO BRUTO]", contentOffsetX, panelY + 22);
        } else {
            g2.setColor(Color.WHITE);
            g2.drawString("THANATOS COLT", contentOffsetX, panelY + 22);
        }

        // Barra de Vida (HP)
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("HP:", contentOffsetX, panelY + 41);
        
        int barW = 200;
        int barH = 11;
        int barX = contentOffsetX + 28;
        int hpY = panelY + 32;

        // Fundo cinza da barra de HP
        g2.setColor(new Color(40, 40, 45));
        g2.fillRect(barX, hpY, barW, barH);
        
        // Preenchimento vermelho proporcional à vida do jogador
        int hpWidth = (int) ((player.getHp() / (double) player.getMaxHp()) * barW);
        g2.setColor(player.isFuriaActive() ? new Color(255, 50, 50) : new Color(220, 40, 40));
        g2.fillRect(barX, hpY, hpWidth, barH);
        
        // Contorno da barra de HP
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(barX, hpY, barW, barH);

        // Texto numérico de HP
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.WHITE);
        g2.drawString(player.getHp() + " / " + player.getMaxHp(), barX + barW + 8, hpY + 9);

        // --------------------------------------------------------------------
        // UI: BARRA DE FÚRIA EM FORMATO DE CHARUTO (CIGAR RAGE BAR)
        // --------------------------------------------------------------------
        int cigarX = barX;
        int cigarY = panelY + 54;
        int cigarTotalW = barW;
        int cigarH = 16;

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("FÚRIA:", contentOffsetX, cigarY + 12);

        drawCigarBar(g2, cigarX, cigarY, cigarTotalW, cigarH);

        // --------------------------------------------------------------------
        // Painel de Informações da Onda (Canto Superior Direito)
        // --------------------------------------------------------------------
        g2.setColor(new Color(10, 10, 20, 220));
        g2.fillRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        g2.setColor(Color.CYAN);
        g2.drawRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("ONDA: " + currentWave + " / 3", WIDTH - 165, 33);
        
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Inimigos: " + enemies.size(), WIDTH - 165, 48);

        // --------------------------------------------------------------------
        // Rodapé com Instruções de Controles
        // --------------------------------------------------------------------
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("[WASD]: Mover | [ESPAÇO]: Cambalhota | [J]: Soco | [K]: Chute | [L]: Modo Fúria | [R]: Reiniciar", 15, HEIGHT - 15);
    }

    /**
     * Desenha a Barra de Fúria em formato de Charuto.
     * Enquanto queima (Modo Fúria ativo), a brasa consome o corpo do charuto da ponta
     * até a base, deixando uma ponta incandescente e fumaça animada.
     */
    private void drawCigarBar(Graphics2D g2, int x, int y, int totalW, int h) {
        double ratio = player.isFuriaActive() ? player.getFuriaPercent() : (player.getFuria() / 100.0);
        int currentW = (int) Math.round(totalW * ratio);

        // Fundo do slot do charuto (silhueta vazia / cinzas)
        g2.setColor(new Color(30, 25, 25, 190));
        g2.fillRoundRect(x, y, totalW, h, 6, 6);
        g2.setColor(new Color(70, 50, 45));
        g2.drawRoundRect(x, y, totalW, h, 6, 6);

        if (currentW > 0) {
            // Corpo de folhas de tabaco do charuto (Gradiente marrom rústico)
            GradientPaint tobaccoGradient = new GradientPaint(
                x, y, new Color(110, 60, 30),
                x, y + h, new Color(75, 40, 20)
            );
            g2.setPaint(tobaccoGradient);
            g2.fillRoundRect(x, y, currentW, h, 6, 6);

            // Anel dourado do charuto (rótulo/cigar band) próximo à base
            int bandX = x + 18;
            int bandW = 10;
            if (currentW > bandX - x + bandW) {
                g2.setColor(new Color(220, 180, 50));
                g2.fillRect(bandX, y, bandW, h);
                g2.setColor(new Color(150, 20, 20));
                g2.fillRect(bandX + 2, y + 2, bandW - 4, h - 4);
            }

            // Linhas de textura das folhas de tabaco enroladas
            g2.setColor(new Color(60, 30, 15, 120));
            for (int lx = x + 35; lx < x + currentW - 8; lx += 14) {
                g2.drawLine(lx, y + 1, lx - 4, y + h - 2);
            }

            // Efeito visual quando a fúria está ativa ou cheia
            int tipX = x + currentW;

            if (player.isFuriaActive()) {
                // Brasa incandescente queimando a ponta
                int emberW = Math.min(10, currentW);
                int emberX = tipX - emberW;
                
                // Ponta de cinzas cinza-escuras
                g2.setColor(new Color(130, 125, 125));
                g2.fillRect(emberX, y + 1, 3, h - 2);

                // Brasa vermelha/laranja pulsante
                long time = System.currentTimeMillis();
                int pulse = (int) (Math.sin(time / 90.0) * 30);
                g2.setColor(new Color(255, 80 + pulse, 20));
                g2.fillRect(emberX + 3, y + 1, emberW - 3, h - 2);

                // Centro da brasa superaquecido em amarelo
                g2.setColor(new Color(255, 230, 100));
                g2.fillRect(emberX + 5, y + 3, 3, h - 6);

                // Fumaça animada subindo da ponta do charuto
                g2.setColor(new Color(220, 220, 230, 90));
                int smokeOffset = (int) ((time / 60) % 15);
                g2.fillOval(tipX - 2 + (smokeOffset % 4), y - 6 - smokeOffset, 5 + (smokeOffset / 3), 4 + (smokeOffset / 3));

                // Faíscas voando
                g2.setColor(new Color(255, 200, 50, 180));
                g2.fillRect(tipX + 2, y + 2 - (int)(time % 6), 2, 2);
            } else if (player.isFuriaPronta()) {
                // Charuto aceso e brilhando dourado pronto para fumar
                g2.setColor(new Color(255, 215, 0));
                g2.drawRoundRect(x, y, totalW, h, 6, 6);
                
                // Brasa pronta na ponta
                g2.setColor(new Color(255, 100, 0));
                g2.fillOval(x + totalW - 8, y + 2, 7, h - 4);
                g2.setColor(new Color(255, 230, 120));
                g2.fillOval(x + totalW - 6, y + 4, 3, h - 8);
            }
        }

        // Borda final do charuto
        g2.setColor(player.isFuriaActive() ? new Color(255, 80, 50) : (player.isFuriaPronta() ? Color.YELLOW : new Color(130, 95, 60)));
        g2.drawRoundRect(x, y, totalW, h, 6, 6);

        // Indicador de comando quando a fúria está pronta
        if (player.isFuriaPronta() && !player.isFuriaActive()) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("ACESO! [Pressione L]", x + totalW + 8, y + 12);
        } else if (player.isFuriaActive()) {
            g2.setColor(new Color(255, 100, 80));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("FÚRIA ATIVA!", x + totalW + 8, y + 12);
        }
    }

    /**
     * Desenha a tela de sobreposição de mensagem em tela cheia (Game Over ou Vitória).
     * 
     * @param g2       Contexto gráfico 2D
     * @param title    Título principal em destaque (ex: "GAME OVER")
     * @param subtitle Frase explicativa complementar
     * @param color    Cor temático do título
     */
    private void drawOverlayMessage(Graphics2D g2, String title, String subtitle, Color color) {
        // Cortina escura semitransparente cobrindo toda a arena
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Título centralizado
        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, 220);

        // Subtítulo descritivo centralizado
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        int subWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (WIDTH - subWidth) / 2, 260);

        // Instrução para reiniciar com a tecla R
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        String restart = "Pressione [R] para jogar novamente";
        int restWidth = g2.getFontMetrics().stringWidth(restart);
        g2.drawString(restart, (WIDTH - restWidth) / 2, 310);
    }

    // ------------------------------------------------------------------------
    // TRATAMENTO DE ENTRADAS DE TECLADO
    // ------------------------------------------------------------------------
    /**
     * Acionado quando qualquer tecla é pressionada.
     * Atualiza os estados de movimentação, desfere ataques ou reinicia o jogo.
     * 
     * @param code Código numérico da tecla pressionada (KeyEvent.VK_*)
     */
    private void handleKeyPressed(int code) {
        // Direcionais (suporta tanto WASD quanto as Setas do teclado)
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = true;

        // Comandos de Ataque e Esquiva
        if (code == KeyEvent.VK_SPACE) {
            // Cambalhota nas 4 direções (W, S, A, D) ou frente
            int rdx = 0, rdy = 0;
            if (keyUp) rdy -= 1;
            if (keyDown) rdy += 1;
            if (keyLeft) rdx -= 1;
            if (keyRight) rdx += 1;
            player.startRoll(rdx, rdy);
        }
        if (code == KeyEvent.VK_J) {
            player.attackSoco(); // Tecla J = Soco
        }
        if (code == KeyEvent.VK_K) {
            player.attackChute(); // Tecla K = Chute
        }
        if (code == KeyEvent.VK_L) {
            // Tecla L = Ativa a Fúria do Dragão se a barra estiver cheia
            if (player.activateFuria()) {
                floatingTexts.add(new FloatingText("FÚRIA DO DRAGÃO!", player.getX() - 30, player.getY() - 30, Color.YELLOW));
            }
        }

        // Tecla R = Reiniciar partida a qualquer momento ou após Game Over / Vitória
        if (code == KeyEvent.VK_R) {
            initGame();
        }
    }

    /**
     * Acionado quando qualquer tecla é liberada pelo usuário.
     * Desativa os estados booleanos de movimento contínuo correspondentes.
     * 
     * @param code Código numérico da tecla solta
     */
    private void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = false;
    }
}
