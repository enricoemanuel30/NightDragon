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
 * ============================================================================
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
    // CONSTANTES DA ARENA DE JOGO (ESPAÇO 2.5D)
    // ------------------------------------------------------------------------
    public static final int WIDTH = 900;       // Largura total da tela em pixels
    public static final int HEIGHT = 500;      // Altura total da tela em pixels
    public static final int GROUND_MIN_Y = 220; // Limite superior da pista de caminhada (horizonte do chão)
    public static final int GROUND_MAX_Y = 440; // Limite inferior da pista de caminhada (frente da tela)

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
        player = new Player(100, 320); // Spawna Thanatos do lado esquerdo da pista
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
        
        // Onda 1: Apresentação básica de combate (1 Pistoleiro e 1 Ceifador)
        if (wave == 1) {
            enemies.add(new PistoleiroEnemy(650, 260));
            enemies.add(new CeifadorEnemy(750, 340));
        } 
        // Onda 2: Aumento da pressão com introdução do Brutamontes Tanque
        else if (wave == 2) {
            enemies.add(new PistoleiroEnemy(700, 240));
            enemies.add(new BrutamontesEnemy(750, 320));
            enemies.add(new CeifadorEnemy(800, 400));
        } 
        // Onda 3: Desafio Final / Boss Wave com 4 inimigos combinados
        else if (wave >= 3) {
            enemies.add(new BrutamontesEnemy(650, 250));
            enemies.add(new PistoleiroEnemy(750, 350));
            enemies.add(new CeifadorEnemy(800, 300));
            enemies.add(new BrutamontesEnemy(850, 420));
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
                    player.addFuria(10);          // Cada acerto com sucesso premia +10 de Fúria
                    
                    // Exibe o número de dano infligido em laranja
                    floatingTexts.add(new FloatingText("-" + dmg, enemy.getX(), enemy.getY() - 15, Color.ORANGE));
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
     * - Painel com nome do herói, barra de HP e barra de Fúria do Dragão
     * - Painel superior direito com contador de ondas e contagem de inimigos
     * - Guia de comandos no rodapé da janela
     */
    private void drawHUD(Graphics2D g2) {
        // --------------------------------------------------------------------
        // Painel do Jogador (Canto Superior Esquerdo)
        // --------------------------------------------------------------------
        // Placa de fundo translúcida com cantos arredondados
        g2.setColor(new Color(10, 10, 20, 220));
        g2.fillRoundRect(15, 15, 340, 80, 15, 15);
        
        // Borda dourada em estilo Velho Oeste tecnológico
        g2.setColor(new Color(180, 140, 60));
        g2.drawRoundRect(15, 15, 340, 80, 15, 15);

        // Nome de Thanatos Colt
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("THANATOS COLT", 25, 35);

        // Barra de Vida (HP)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("HP:", 25, 53);
        
        // Fundo cinza da barra de HP
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(50, 43, 200, 12);
        
        // Preenchimento vermelho proporcional à vida do jogador
        int hpWidth = (int) ((player.getHp() / (double) player.getMaxHp()) * 200);
        g2.setColor(new Color(220, 40, 40));
        g2.fillRect(50, 43, hpWidth, 12);
        
        // Contorno branco da barra de HP
        g2.setColor(Color.WHITE);
        g2.drawRect(50, 43, 200, 12);

        // Barra de FÚRIA DO DRAGÃO
        g2.drawString("FÚRIA:", 25, 73);
        
        // Fundo cinza da barra de Fúria
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(50, 63, 200, 12);
        
        // Preenchimento da barra de Fúria (alaranjado durante carregamento, dourado quando pronta)
        int furiaWidth = (int) ((player.getFuria() / 100.0) * 200);
        g2.setColor(player.isFuriaPronta() ? new Color(255, 200, 0) : new Color(255, 120, 0));
        g2.fillRect(50, 63, furiaWidth, 12);
        
        // Contorno da barra de fúria
        g2.setColor(Color.WHITE);
        g2.drawRect(50, 63, 200, 12);

        // Indicador de golpe supremo disponível
        if (player.isFuriaPronta()) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("PRONTA! (Pressione L)", 260, 73);
        }

        // --------------------------------------------------------------------
        // Painel de Informações da Onda (Canto Superior Direito)
        // --------------------------------------------------------------------
        g2.setColor(new Color(10, 10, 20, 220));
        g2.fillRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        g2.setColor(Color.CYAN);
        g2.drawRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        
        // Número da onda
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("ONDA: " + currentWave + " / 3", WIDTH - 165, 33);
        
        // Inimigos restantes vivos na tela
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Inimigos: " + enemies.size(), WIDTH - 165, 48);

        // --------------------------------------------------------------------
        // Rodapé com Instruções de Controles
        // --------------------------------------------------------------------
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("[WASD]: Mover | [J]: Soco | [K]: Chute | [L]: Fúria do Dragão | [R]: Reiniciar", 15, HEIGHT - 15);
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

        // Comandos de Ataque
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
