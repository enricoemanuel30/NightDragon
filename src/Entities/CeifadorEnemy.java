package Entities;

// Importa a classe Projectile para assinatura compatível com updateAI
import main.engine.Projectile;

// Importa classes gráficas do AWT para cores, renderização e geometria
import java.awt.*;

// Importa interface List para coleções
import java.util.List;

/**
 * ============================================================================
 * INIMIGO CONCRETO: CEIFADOR (DEVIL COWBOY REAPER / ASSASSINO CIBERNÉTICO)
 * ============================================================================
 * Subclasse de Enemy especializada em agilidade extrema:
 * - Corpo esguio e pernas cibernéticas hidráulicas de alta velocidade (speed 3)
 * - Menos pontos de vida (75 HP)
 * - Ataque rápido com lâminas de plasma neon verde nos antebraços (18 de dano).
 * ============================================================================
 */
public class CeifadorEnemy extends Enemy {

    /**
     * Construtor do Ceifador.
     * Configura um corpo mais ágil e esguio (30x70), vida intermediária (75 HP) e velocidade alta (3).
     * 
     * @param x Posição X inicial
     * @param y Posição Y inicial
     */
    public CeifadorEnemy(int x, int y) {
        // super(x, y, largura, altura, vida, velocidade)
        super(x, y, 30, 70, 75, 3);
        this.attackDamage = 18; // Dano cortante da lâmina de plasma
    }

    /**
     * Inteligência Artificial do Ceifador:
     * - Movimento veloz em perseguição direta ao jogador.
     * - Corta distâncias rapidamente no eixo vertical (Y) com sua alta velocidade.
     * - Ataca rapidamente com curto tempo de recarga (cooldown de 50 frames ~ 0.8s).
     * 
     * @param player      Referência ao jogador Thanatos Colt
     * @param projectiles Lista de projéteis (não utilizada, pois Ceifador é combate corpo a corpo rápido)
     */
    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        // Mira e vira o rosto/corpo na direção de Thanatos
        facingRight = player.getX() > x;

        // 1. Aproximação horizontal rápida (X)
        if (Math.abs(player.getX() - x) > 30) {
            x += (player.getX() > x) ? speed : -speed;
        }

        // 2. Alinhamento de profundidade (Y) em alta velocidade
        // Utiliza 'speed' também no eixo Y para criar investidas rápidas em diagonal
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? speed : -speed;
        }

        // 3. Golpe de Lâmina Cibernética
        // Acionado quando estiver no raio de 40px horizontais e 15px verticais
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 40 && Math.abs(player.getY() - y) < 15) {
            attacking = true;      // Inicia o corte com a lâmina
            hitboxActive = true;   // Habilita a área cortante para causar dano
            attackTimer = 14;      // Animação rápida e incisiva de apenas 14 frames
            attackCooldown = 50;   // Cooldown menor que o do Brutamontes (ataques mais frequentes)
        }
    }

    /**
     * Renderização visual do Ceifador:
     * Desenha a silhueta ágil verde venenosa e o polígono triangular da lâmina neon energizada.
     * 
     * @param g2 Contexto gráfico 2D
     */
    @Override
    public void render(Graphics2D g2) {
        // Converte a âncora inferior central para o canto superior esquerdo
        int drawX = x - width / 2;
        int drawY = y - height;

        // 1. Sombra alongada no solo
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 15, y - 6, 30, 12);

        // 2. Traje Tecnológico e Pernas Mecânicas (Tom Verde Veneno Cyberpunk)
        g2.setColor(new Color(20, 80, 60));
        g2.fillRect(drawX + 5, drawY + 15, width - 10, height - 15);

        // 3. Lâmina Neon Verde nos Antebraços (Efeito de corte em formato de triângulo)
        g2.setColor(new Color(50, 255, 120)); // Verde brilhante neon
        int bladeX = facingRight ? drawX + width : drawX - 20;
        
        // Se estiver atacando, desenha a lâmina projetada para frente como um triângulo pontiagudo
        if (attacking) {
            // Pontos X e Y que formam os 3 vértices do triângulo da lâmina
            int[] polyX = new int[]{ bladeX, bladeX + (facingRight ? 25 : -25), bladeX };
            int[] polyY = new int[]{ drawY + 20, drawY + 35, drawY + 50 };
            g2.fillPolygon(polyX, polyY, 3);
        }

        // 4. Barra de Vida (HP) sobre a cabeça
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 12, width, 5); // Moldura preta
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width); // Proporção da vida atual
        g2.fillRect(drawX, drawY - 12, hpW, 5);
    }
}
