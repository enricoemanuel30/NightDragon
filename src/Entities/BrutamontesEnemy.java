package Entities;

// Importa a classe Projectile (necessária para assinatura de updateAI herdada de Enemy)
import main.engine.Projectile;

// Importa classes gráficas do AWT para desenhar formas, cores e sombras
import java.awt.*;

// Importa a interface List para lidar com coleções
import java.util.List;

/**
 * ============================================================================
 * INIMIGO CONCRETO: BRUTAMONTES (DEVIL COWBOY BRUTE - O TANQUE MECÂNICO)
 * ============================================================================
 * Subclasse de Enemy. Representa um inimigo cibernético altamente modificado:
 * - Alta resistência (140 HP)
 * - Movimento lento (speed 1)
 * - Ataque corpo a corpo devastador: Soco Hidráulico causando 25 de dano.
 * ============================================================================
 */
public class BrutamontesEnemy extends Enemy {

    /**
     * Construtor do Brutamontes.
     * Define as dimensões robustas (48x80), 140 de HP e velocidade 1.
     * 
     * @param x Posição X de spawn
     * @param y Posição Y de spawn
     */
    public BrutamontesEnemy(int x, int y) {
        // super(x, y, largura, altura, vida, velocidade)
        super(x, y, 48, 80, 140, 1); 
        this.attackDamage = 25; // Dano massivo do soco mecânico
    }

    /**
     * Implementação da Inteligência Artificial do Brutamontes:
     * - Sempre se vira em direção ao jogador.
     * - Persegue Thanatos diretamente nos eixos X e Y até encurtar a distância.
     * - Desfere o soco pesado quando estiver bem próximo no espaço 2.5D.
     * 
     * @param player      Referência ao protagonista Thanatos Colt
     * @param projectiles Lista de projéteis (não utilizada pelo Brutamontes, pois ele é combate corpo a corpo)
     */
    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        // Orientação visual: vira para a direita se o jogador estiver à direita
        facingRight = player.getX() > x;

        // 1. Perseguição no eixo horizontal (X)
        // Mantém uma pequena margem (35px) para não sobrepor completamente o sprite do jogador
        if (Math.abs(player.getX() - x) > 35) {
            // Se o jogador estiver à frente, soma velocidade; se estiver atrás, subtrai
            x += (player.getX() > x) ? speed : -speed;
        }

        // 2. Perseguição no eixo de profundidade (Y)
        // Alinha a linha do chão com o jogador com tolerância de 5 pixels
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
        }

        // 3. Checagem de Alcance e Disparo do Soco Mecânico Pesado
        // Condições: tempo de recarga zerado E proximidade em X (<= 45px) E mesmo plano de profundidade em Y (< 15px)
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 45 && Math.abs(player.getY() - y) < 15) {
            attacking = true;      // Entra em estado de animação de ataque
            hitboxActive = true;   // Ativa a área de impacto para ferir o jogador
            attackTimer = 25;      // Animação dura 25 frames
            attackCooldown = 80;   // Cooldown longo entre golpes (aprox. 1.3 segundos)
        }
    }

    /**
     * Renderização procedural do Brutamontes:
     * Desenha a sombra projetada no chão, corpo de aço reforçado, pistão vermelho e braços hidráulicos.
     * 
     * @param g2 Contexto gráfico 2D
     */
    @Override
    public void render(Graphics2D g2) {
        // Calcula as coordenadas do canto superior esquerdo a partir do ponto central dos pés (x, y)
        int drawX = x - width / 2;
        int drawY = y - height;

        // 1. Sombra Larga elíptica no chão para dar efeito de contato físico com a pista 2.5D
        g2.setColor(new Color(0, 0, 0, 100)); // Preto translúcido
        g2.fillOval(x - 25, y - 8, 50, 16);

        // 2. Tronco e Corpo Robusto (Textura visual de chapa de aço industrial cinza-escura)
        g2.setColor(new Color(60, 65, 70));
        g2.fillRect(drawX, drawY, width, height);

        // 3. Pistão Mecânico Vermelho de Alimentação no Centro do Peito
        g2.setColor(Color.RED);
        g2.fillRect(drawX + 16, drawY + 20, 16, 16);

        // 4. Braços Hidráulicos Gigantes (Aço cromado/cinza claro)
        g2.setColor(new Color(150, 150, 160));
        if (attacking) {
            // Desenha o punho mecânico estendido para frente durante o golpe
            int fistX = facingRight ? drawX + width : drawX - 25;
            g2.fillRect(fistX, drawY + 25, 25, 25);
        } else {
            // Desenha os dois braços em posição neutra lateral
            g2.fillRect(drawX - 8, drawY + 20, 10, 35);           // Braço esquerdo
            g2.fillRect(drawX + width - 2, drawY + 20, 10, 35);   // Braço direito
        }

        // 5. Barra de Pontos de Vida (HP) posicionada acima da cabeça do inimigo
        // Fundo preto da barra
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 12, width, 6);
        
        // Preenchimento vermelho proporcional à vida restante: (hp / maxHp) * largura total
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY - 12, hpW, 6);
    }
}
