package main.engine;

import java.awt.*;

/**
 * ============================================================================
 * SISTEMA DE PROJÉTEIS (TIROS DO PISTOLEIRO)
 * ============================================================================
 * Projéteis se movem horizontalmente e são desativados ao sair da tela
 * ou ao colidir com o jogador (verificado no GamePanel).
 * ============================================================================
 */
public class Projectile {
    private int x, y;
    private int vx;
    private int damage;
    private boolean active = true;

    public Projectile(int x, int y, int vx, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.damage = damage;
    }

    public void update() {
        x += vx;
        if (x < 0 || x > GamePanel.WIDTH) {
            active = false;
        }
    }

    public void render(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        g2.fillOval(x - 4, y - 4, 8, 8);
        g2.setColor(Color.ORANGE);
        g2.drawOval(x - 6, y - 6, 12, 12);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 4, y - 4, 8, 8);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDamage() { return damage; }
}
