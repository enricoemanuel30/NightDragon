package Entities;

import main.engine.Projectile;
import java.awt.*;
import java.util.List;

/**
 * ============================================================================
 * INIMIGO 3: CEIFADOR (Pernas hidráulicas e lâminas nos braços)
 * ============================================================================
 * Muito rápido (speed 3). Persegue o jogador com ziguezague e ataca
 * com lâminas neon a cada 0.8s causando 18 de dano.
 * ============================================================================
 */
public class CeifadorEnemy extends Enemy {

    public CeifadorEnemy(int x, int y) {
        super(x, y, 30, 70, 75, 3); // Muito rápido
        this.attackDamage = 18;
    }

    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        facingRight = player.getX() > x;

        // Movimento rápido de ziguezague
        if (Math.abs(player.getX() - x) > 30) {
            x += (player.getX() > x) ? speed : -speed;
        }
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? speed : -speed;
        }

        // Ataque Rápido de Lâmina
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 40 && Math.abs(player.getY() - y) < 15) {
            attacking = true;
            hitboxActive = true;
            attackTimer = 14;
            attackCooldown = 50;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 15, y - 6, 30, 12);

        // Traje Esbelto com Pernas Mecânicas
        g2.setColor(new Color(20, 80, 60)); // Verde Veneno Cyberpunk
        g2.fillRect(drawX + 5, drawY + 15, width - 10, height - 15);

        // Lâmina Neon Verde nos Antebraços
        g2.setColor(new Color(50, 255, 120));
        int bladeX = facingRight ? drawX + width : drawX - 20;
        if (attacking) {
            g2.fillPolygon(new int[]{bladeX, bladeX + (facingRight ? 25 : -25), bladeX},
                           new int[]{drawY + 20, drawY + 35, drawY + 50}, 3);
        }

        // Barra de HP
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 12, width, 5);
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY - 12, hpW, 5);
    }
}
