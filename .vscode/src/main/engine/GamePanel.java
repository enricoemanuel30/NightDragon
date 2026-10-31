package main.engine;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import Entities.*;

/**
 * ============================================================================
 * PAINEL PRINCIPAL DO JOGO (GAME LOOP, INPUTS E RENDERIZAÇÃO)
 * ============================================================================
 */
public class GamePanel extends JPanel implements Runnable {

    // Dimensões da Arena de Jogo (2.5D Beat 'em Up)
    public static final int WIDTH = 900;
    public static final int HEIGHT = 500;
    public static final int GROUND_MIN_Y = 220; // Limite superior da pista de caminhada
    public static final int GROUND_MAX_Y = 440; // Limite inferior da pista de caminhada

    // Loop do Jogo (Target 60 FPS = ~16ms por frame)
    private Thread gameThread;
    private boolean running = false;
    private final int FPS = 60;
    private final long TARGET_TIME = 1000 / FPS;

    // Entidades do Jogo
    private Player player;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private List<FloatingText> floatingTexts;

    // Estado do Jogo
    private int currentWave = 1;
    private boolean gameOver = false;
    private boolean victory = false;

    // Controle de Teclas
    private boolean keyUp, keyDown, keyLeft, keyRight;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 20, 30)); // Fundo escuro Cyberpunk
        setFocusable(true);

        initGame();

        // Listener de Teclado
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e.getKeyCode());
            }
        });
    }

    private void initGame() {
        player = new Player(100, 320);
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();
        floatingTexts = new ArrayList<>();
        gameOver = false;
        victory = false;
        currentWave = 1;

        spawnWave(currentWave);
    }

    private void spawnWave(int wave) {
        enemies.clear();
        projectiles.clear();
        
        // Quantidade progressiva de inimigos por onda
        if (wave == 1) {
            enemies.add(new PistoleiroEnemy(650, 260));
            enemies.add(new CeifadorEnemy(750, 340));
        } else if (wave == 2) {
            enemies.add(new PistoleiroEnemy(700, 240));
            enemies.add(new BrutamontesEnemy(750, 320));
            enemies.add(new CeifadorEnemy(800, 400));
        } else if (wave >= 3) {
            // Onda de Chefe / Desafio Final
            enemies.add(new BrutamontesEnemy(650, 250));
            enemies.add(new PistoleiroEnemy(750, 350));
            enemies.add(new CeifadorEnemy(800, 300));
            enemies.add(new BrutamontesEnemy(850, 420));
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        long start, elapsed, wait;

        while (running) {
            start = System.currentTimeMillis();

            updateGame();
            repaint();

            elapsed = System.currentTimeMillis() - start;
            wait = TARGET_TIME - elapsed;

            if (wait < 0) wait = 5;

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------------------
    // ATUALIZAÇÃO DA LÓGICA DO JOGO
    // ------------------------------------------------------------------------
    private void updateGame() {
        if (gameOver || victory) return;

        // 1. Processar Movimentação do Jogador
        int dx = 0, dy = 0;
        if (keyUp) dy -= 1;
        if (keyDown) dy += 1;
        if (keyLeft) dx -= 1;
        if (keyRight) dx += 1;
        player.move(dx, dy);

        // 2. Atualizar Jogador
        player.update();

        // 3. Atualizar Projéteis
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update();
            if (!p.isActive()) {
                projIt.remove();
                continue;
            }

            // Colisão de projétil com o Jogador
            if (p.getBounds().intersects(player.getBounds())) {
                player.takeDamage(p.getDamage());
                floatingTexts.add(new FloatingText("-" + p.getDamage(), player.getX(), player.getY() - 10, Color.RED));
                p.setActive(false);
                projIt.remove();

                if (player.getHp() <= 0) {
                    gameOver = true;
                }
            }
        }

        // 4. Atualizar Inimigos e IA
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            if (!enemy.isAlive()) {
                // Ganha Fúria ao derrotar inimigo
                player.addFuria(25);
                floatingTexts.add(new FloatingText("+25 FÚRIA", enemy.getX(), enemy.getY() - 20, Color.YELLOW));
                enemyIt.remove();
                continue;
            }

            enemy.updateAI(player, projectiles);
            enemy.update();

            // Colisão de ataque do inimigo contra o Jogador
            if (enemy.isAttacking() && enemy.hasHitboxActive() && enemy.getAttackBounds().intersects(player.getBounds())) {
                if (!player.isInvulnerable()) {
                    player.takeDamage(enemy.getAttackDamage());
                    floatingTexts.add(new FloatingText("-" + enemy.getAttackDamage(), player.getX(), player.getY() - 10, Color.RED));
                    enemy.setHitboxActive(false);

                    if (player.getHp() <= 0) {
                        gameOver = true;
                    }
                }
            }

            // Colisão do ataque de Thanatos contra este inimigo
            if (player.isAttacking() && player.hasHitboxActive()) {
                if (player.getAttackBounds().intersects(enemy.getBounds())) {
                    int dmg = player.getAttackDamage();
                    enemy.takeDamage(dmg);
                    player.addFuria(10); // Cada acerto gera Fúria
                    floatingTexts.add(new FloatingText("-" + dmg, enemy.getX(), enemy.getY() - 15, Color.ORANGE));
                }
            }
        }

        // Desativar hitbox do jogador após checar todos inimigos no frame
        if (player.isAttacking() && player.hasHitboxActive()) {
            player.setHitboxActive(false);
        }

        // 5. Atualizar Textos Flutuantes
        Iterator<FloatingText> ftIt = floatingTexts.iterator();
        while (ftIt.hasNext()) {
            FloatingText ft = ftIt.next();
            ft.update();
            if (!ft.isActive()) ftIt.remove();
        }

        // 6. Checar Progressão de Onda (Wave)
        if (enemies.isEmpty()) {
            currentWave++;
            if (currentWave > 3) {
                victory = true;
            } else {
                spawnWave(currentWave);
                floatingTexts.add(new FloatingText("ONDA " + currentWave + "!", WIDTH / 2 - 50, 150, Color.CYAN));
            }
        }
    }

    // ------------------------------------------------------------------------
    // RENDERIZAÇÃO GRÁFICA DA CENA
    // ------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // A. Desenhar Cenário (Estética Velho Oeste Cyberpunk / Cidade do Milênio)
        drawBackground(g2);

        // B. Ordenação Y (Depth Sorting / Efeito 2.5D do Beat 'em Up)
        List<Entity> renderList = new ArrayList<>();
        renderList.add(player);
        renderList.addAll(enemies);
        renderList.sort((e1, e2) -> Integer.compare(e1.getY(), e2.getY()));

        // Desenhar Entidades ordenadas pela profundidade Y
        for (Entity e : renderList) {
            e.render(g2);
        }

        // C. Desenhar Projéteis
        for (Projectile p : projectiles) {
            p.render(g2);
        }

        // D. Desenhar Textos Flutuantes (Dano/Status)
        for (FloatingText ft : floatingTexts) {
            ft.render(g2);
        }

        // E. Desenhar HUD / Interface do Usuário
        drawHUD(g2);

        // F. Desenhar Telas de Fim de Jogo (GameOver / Vitória)
        if (gameOver) {
            drawOverlayMessage(g2, "GAME OVER", "Thanatos foi derrotado pelos Devil Cowboys!", Color.RED);
        } else if (victory) {
            drawOverlayMessage(g2, "VITÓRIA DA NOITE!", "Você eliminou os Devil Cowboys da Cidade do Milênio!", Color.GREEN);
        }
    }

    private void drawBackground(Graphics2D g2) {
        // Céu Noturno Tecnológico
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(15, 10, 30), 0, GROUND_MIN_Y, new Color(40, 20, 60));
        g2.setPaint(skyGradient);
        g2.fillRect(0, 0, WIDTH, GROUND_MIN_Y);

        // Silhueta de Prédios Cyberpunk com Luzes Neo-Vitorianas
        g2.setColor(new Color(25, 20, 40));
        g2.fillRect(50, 60, 100, GROUND_MIN_Y - 60);
        g2.fillRect(200, 40, 140, GROUND_MIN_Y - 40);
        g2.fillRect(400, 80, 120, GROUND_MIN_Y - 80);
        g2.fillRect(600, 30, 160, GROUND_MIN_Y - 30);

        // Janelas Neon
        g2.setColor(new Color(255, 0, 128, 180)); // Rosa Neon
        g2.fillRect(220, 70, 15, 25);
        g2.fillRect(250, 70, 15, 25);
        g2.setColor(new Color(0, 230, 255, 180)); // Azul Neon
        g2.fillRect(630, 60, 20, 20);
        g2.fillRect(680, 60, 20, 20);

        // Pista de Combate (Chão de Poeira Mecânica / Asfalto)
        GradientPaint groundGradient = new GradientPaint(0, GROUND_MIN_Y, new Color(60, 45, 40), 0, HEIGHT, new Color(25, 20, 20));
        g2.setPaint(groundGradient);
        g2.fillRect(0, GROUND_MIN_Y, WIDTH, HEIGHT - GROUND_MIN_Y);

        // Linhas de Perspectiva do Solo
        g2.setColor(new Color(100, 70, 50, 100));
        g2.drawLine(0, GROUND_MIN_Y, WIDTH, GROUND_MIN_Y);
        g2.drawLine(0, GROUND_MAX_Y, WIDTH, GROUND_MAX_Y);
    }

    private void drawHUD(Graphics2D g2) {
        // Moldura do HUD (Superior Esquerdo)
        g2.setColor(new Color(10, 10, 20, 220));
        g2.fillRoundRect(15, 15, 340, 80, 15, 15);
        g2.setColor(new Color(180, 140, 60)); // Bordas Douradas Velho Oeste
        g2.drawRoundRect(15, 15, 340, 80, 15, 15);

        // Nome do Protagonista
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("THANATOS COLT", 25, 35);

        // Barra de HP (Vida)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("HP:", 25, 53);
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(50, 43, 200, 12);
        int hpWidth = (int) ((player.getHp() / (double) player.getMaxHp()) * 200);
        g2.setColor(new Color(220, 40, 40));
        g2.fillRect(50, 43, hpWidth, 12);
        g2.setColor(Color.WHITE);
        g2.drawRect(50, 43, 200, 12);

        // Barra de FÚRIA DO DRAGÃO
        g2.drawString("FÚRIA:", 25, 73);
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(50, 63, 200, 12);
        int furiaWidth = (int) ((player.getFuria() / 100.0) * 200);
        g2.setColor(player.isFuriaPronta() ? new Color(255, 200, 0) : new Color(255, 120, 0));
        g2.fillRect(50, 63, furiaWidth, 12);
        g2.setColor(Color.WHITE);
        g2.drawRect(50, 63, 200, 12);

        if (player.isFuriaPronta()) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("PRONTA! (Pressione L)", 260, 73);
        }

        // Informações da Onda (Superior Direito)
        g2.setColor(new Color(10, 10, 20, 220));
        g2.fillRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        g2.setColor(Color.CYAN);
        g2.drawRoundRect(WIDTH - 180, 15, 165, 45, 15, 15);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("ONDA: " + currentWave + " / 3", WIDTH - 165, 33);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Inimigos: " + enemies.size(), WIDTH - 165, 48);

        // Painel de Comandos (Rodapé)
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("[WASD]: Mover | [J]: Soco | [K]: Chute | [L]: Fúria do Dragão | [R]: Reiniciar", 15, HEIGHT - 15);
    }

    private void drawOverlayMessage(Graphics2D g2, String title, String subtitle, Color color) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, 220);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        int subWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (WIDTH - subWidth) / 2, 260);

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        String restart = "Pressione [R] para jogar novamente";
        int restWidth = g2.getFontMetrics().stringWidth(restart);
        g2.drawString(restart, (WIDTH - restWidth) / 2, 310);
    }

    // ------------------------------------------------------------------------
    // MANIPULAÇÃO DE ENTRADAS DE TECLADO
    // ------------------------------------------------------------------------
    private void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = true;

        // Ataques
        if (code == KeyEvent.VK_J) player.attackSoco();
        if (code == KeyEvent.VK_K) player.attackChute();
        if (code == KeyEvent.VK_L) {
            if (player.activateFuria()) {
                floatingTexts.add(new FloatingText("FÚRIA DO DRAGÃO!", player.getX() - 30, player.getY() - 30, Color.YELLOW));
            }
        }

        // Reiniciar Jogo
        if (code == KeyEvent.VK_R) {
            initGame();
        }
    }

    private void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = false;
    }
}
