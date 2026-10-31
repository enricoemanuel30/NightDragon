package Entities;

import main.engine.Projectile;
import java.awt.*;
import java.util.List;

/**
 * ============================================================================
 * INIMIGO 1: PISTOLEIRO (Braço mecânico com revólver - Ataque à Distância)
 * ============================================================================
 * Mantém distância do jogador e dispara projéteis a cada 1.5s.
 * Se o jogador se aproximar demais, recua.
 * ============================================================================
 */
public class PistoleiroEnemy extends Enemy {

    public PistoleiroEnemy(int x, int y) {
        super(x, y, 32, 65, 50, 2);
    }

    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        // Tenta manter uma distância de tiro de Thanatos
        int distanceX = Math.abs(player.getX() - x);
        facingRight = player.getX() > x;

        if (distanceX > 250) {
            x += (player.getX() > x) ? speed : -speed;
        } else if (distanceX < 150) {
            x += (player.getX() > x) ? -speed : speed; // Recua se estiver muito perto
        }

        // Alinha eixo Y para atirar na altura certa
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
        }

        // Disparo à distância
        if (attackCooldown <= 0 && distanceX <= 300 && Math.abs(player.getY() - y) < 20) {
            attacking = true;
            attackTimer = 15;
            attackCooldown = 90; // Atira a cada 1.5s

            int bulletSpeed = facingRight ? 7 : -7;
            projectiles.add(new Projectile(x, y - 40, bulletSpeed, 10));
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 18, y - 6, 36, 12);

        // Corpo (Traje de Cowboy Cibernético)
        g2.setColor(new Color(80, 50, 30)); // Marrom Couro
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        // Chapéu de Cowboy Tecno
        g2.setColor(new Color(40, 30, 20));
        g2.fillRect(drawX - 4, drawY - 4, width + 8, 6);
        g2.fillRect(drawX + 6, drawY - 12, 20, 10);

        // Braço Mecânico Revolver (Cobre / Bronze Cyberpunk)
        g2.setColor(new Color(200, 120, 40));
        int gunX = facingRight ? drawX + width : drawX - 15;
        g2.fillRect(gunX, drawY + 22, 15, 8);

        // Barra de Vida Individual do Inimigo
        drawEnemyHP(g2, drawX, drawY);
    }

    private void drawEnemyHP(Graphics2D g2, int drawX, int drawY) {
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY - 18, width, 5);
        g2.setColor(Color.RED);
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY - 18, hpW, 5);
    }
}
