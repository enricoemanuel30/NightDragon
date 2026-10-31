package Entities;

import main.engine.Projectile;
import java.awt.*;
import java.util.List;

/**
 * ============================================================================
 * INIMIGO 2: BRUTAMONTES (Braços mecânicos pesados - Tanque de Combate)
 * ============================================================================
 * Lento mas muito resistente (140 HP). Persegue o jogador e ataca
 * com soco pesado de 25 de dano.
 * ============================================================================
 */
public class BrutamontesEnemy extends Enemy {

    public BrutamontesEnemy(int x, int y) {
        super(x, y, 48, 80, 140, 1); // Lento, mas muito resistente
        this.attackDamage = 25;
    }

    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        facingRight = player.getX() > x;

        // Perseguição Direta
        if (Math.abs(player.getX() - x) > 35) {
            x += (player.getX() > x) ? speed : -speed;
        }
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
        }

        // Ataque de Soco Pesado Mecânico
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 45 && Math.abs(player.getY() - y) < 15) {
            attacking = true;
            hitboxActive = true;
            attackTimer = 25;
            attackCooldown = 80;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        // Sombra Larga
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 25, y - 8, 50, 16);

        // Corpo Robusto
        g2.setColor(new Color(60, 65, 70)); // Chapa de Aço Industrial
        g2.fillRect(drawX, drawY, width, height);

        // Piston Mecânico Vermelho no Peito
        g2.setColor(Color.RED);
        g2.fillRect(drawX + 16, drawY + 20, 16, 16);

        // Braços Hidráulicos Gigantes
        g2.setColor(new Color(150, 150, 160));
        if (attacking) {
            int fistX = facingRight ? drawX + width : drawX - 25;
            g2.fillRect(fistX, drawY + 25, 25, 25);
        } else {
            g2.fillRect(drawX - 8, drawY + 20, 10, 35);
            g2.fillRect(drawX + width - 2, drawY + 20, 10, 35);
        }

        // Barra de HP
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 12, width, 6);
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY - 12, hpW, 6);
    }
}
