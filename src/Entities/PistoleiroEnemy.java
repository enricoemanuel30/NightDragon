package Entities;

// Importa a classe Projectile para instanciar as balas disparadas pelo revólver cibernético
import main.engine.Projectile;

// Importa classes gráficas do AWT para cores, renderização e desenhos
import java.awt.*;

// Importa a interface List para interagir com a lista de projéteis da arena
import java.util.List;

/**
 * ============================================================================
 * INIMIGO CONCRETO: PISTOLEIRO (DEVIL COWBOY GUNNER - ATIRADOR À DISTÂNCIA)
 * ============================================================================
 * Subclasse de Enemy especializada em combate com armas de fogo à distância:
 * - Mantém distância de segurança de Thanatos Colt (zona de conforto entre 150 e 250 pixels)
 * - Recua se o jogador tentar encurralá-lo
 * - Dispara tiros que atravessam a tela em linha reta na mesma faixa de profundidade (Y).
 * ============================================================================
 */
public class PistoleiroEnemy extends Enemy {

    /**
     * Construtor do Pistoleiro.
     * Define tamanho (32x65), vida baixa (50 HP) e velocidade moderada (speed 2).
     * 
     * @param x Posição X inicial
     * @param y Posição Y inicial
     */
    public PistoleiroEnemy(int x, int y) {
        // super(x, y, largura, altura, vida, velocidade)
        super(x, y, 32, 65, 50, 2);
    }

    /**
     * Inteligência Artificial do Pistoleiro:
     * - Calcula a distância horizontal até o jogador.
     * - Aplica o conceito de "Kiting" (manter distância tática):
     *   * Se estiver longe demais (>250px), avança.
     *   * Se Thanatos chegar perto demais (<150px), recua estrategicamente.
     * - Alinha a coordenada Y com o jogador para não errar o disparo no plano 2.5D.
     * - Dispara um projétil em direção a Thanatos Colt.
     * 
     * @param player      Referência ao protagonista Thanatos Colt
     * @param projectiles Coleção de projéteis do GamePanel onde a nova bala será adicionada
     */
    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        // Distância absoluta em pixels no eixo horizontal
        int distanceX = Math.abs(player.getX() - x);

        // Vira para a direção em que Thanatos se encontra
        facingRight = player.getX() > x;

        // 1. Manutenção de Distância Tática (Eixo X)
        if (distanceX > 250) {
            // Avança na direção do jogador para entrar no alcance de tiro
            x += (player.getX() > x) ? speed : -speed;
        } else if (distanceX < 150) {
            // Recua para o lado oposto se o jogador se aproximar perigosamente
            x += (player.getX() > x) ? -speed : speed;
        }

        // 2. Alinhamento de Mira no Eixo de Profundidade (Y)
        // O tiro só atinge o jogador se ambos estiverem na mesma faixa de chão (2.5D)
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
        }

        // 3. Mecânica de Disparo de Projétil à Distância
        // Dispara se: recarga zerada E dentro do alcance de 300px E alinhado em Y com Thanatos (<20px de diferença)
        if (attackCooldown <= 0 && distanceX <= 300 && Math.abs(player.getY() - y) < 20) {
            attacking = true;     // Exibe a pose de tiro
            attackTimer = 15;     // Duração do recuo/animação do tiro
            attackCooldown = 90;  // Cadência de tiro: 90 frames = 1.5 segundos entre cada disparo

            // Define a velocidade da bala (+7 para a direita, -7 para a esquerda)
            int bulletSpeed = facingRight ? 7 : -7;

            // Cria uma nova bala na altura do peito do pistoleiro (y - 40) causando 10 de dano
            projectiles.add(new Projectile(x, y - 40, bulletSpeed, 10));
        }
    }

    /**
     * Renderização visual temática do Pistoleiro:
     * Desenha a sombra, sobretudo de couro marrom, chapéu de cowboy cibernético,
     * braço de bronze com o cano do revólver e a barra de vida.
     * 
     * @param g2 Contexto gráfico 2D
     */
    @Override
    public void render(Graphics2D g2) {
        // Conversão de âncora de pés para topo-esquerdo
        int drawX = x - width / 2;
        int drawY = y - height;

        // 1. Sombra elíptica no chão
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 18, y - 6, 36, 12);

        // 2. Traje / Sobretudo de Couro Cibernético (Tom Marrom Velho Oeste)
        g2.setColor(new Color(80, 50, 30));
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        // 3. Chapéu de Cowboy Tecno (Aba e Copa estilizadas)
        g2.setColor(new Color(40, 30, 20)); // Marrom escuro
        g2.fillRect(drawX - 4, drawY - 4, width + 8, 6); // Aba do chapéu
        g2.fillRect(drawX + 6, drawY - 12, 20, 10);     // Copa do chapéu

        // 4. Braço Mecânico Revolver (Tom de Cobre / Bronze Cyberpunk)
        g2.setColor(new Color(200, 120, 40));
        int gunX = facingRight ? drawX + width : drawX - 15;
        g2.fillRect(gunX, drawY + 22, 15, 8); // Desenha o cano do revólver estendido

        // 5. Barra de Vida Individual sobre a cabeça
        drawEnemyHP(g2, drawX, drawY);
    }

    /**
     * Método auxiliar privado para desenhar a barra de vida específica do Pistoleiro.
     * 
     * @param g2    Contexto gráfico 2D
     * @param drawX Posição horizontal de desenho
     * @param drawY Posição vertical de desenho
     */
    private void drawEnemyHP(Graphics2D g2, int drawX, int drawY) {
        // Fundo preto da barra
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 18, width, 5);

        // Preenchimento vermelho proporcional aos pontos de vida restantes
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY - 18, hpW, 5);
    }
}
