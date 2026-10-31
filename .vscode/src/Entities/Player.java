package Entities;

import main.engine.GamePanel;
import java.awt.*;

/**
 * ============================================================================
 * PROTAGONISTA: THANATOS COLT (PLAYER)
 * ============================================================================
 * Herda de Entity. Gerencia movimento na arena 2.5D, sistema de combate
 * (Soco / Chute / Fúria do Dragão) e a barra de Fúria.
 * ============================================================================
 */
public class Player extends Entity {

    private int furia = 0; // Barra de 0 a 100
    private boolean isFuriaActive = false;
    private int furiaTimer = 0;
    private int attackType = 1; // 1 = Soco, 2 = Chute, 3 = Fúria

    public Player(int x, int y) {
        super(x, y, 36, 70, 100, 4);
    }

    public void move(int dx, int dy) {
        if (attacking && !isFuriaActive) return; // Fica imóvel durante ataque normal

        if (dx > 0) { x += speed; facingRight = true; }
        if (dx < 0) { x -= speed; facingRight = false; }
        if (dy > 0) { y += speed; }
        if (dy < 0) { y -= speed; }

        // Restrição de limites da arena (2.5D)
        if (x < 30) x = 30;
        if (x > GamePanel.WIDTH - 30) x = GamePanel.WIDTH - 30;
        if (y < GamePanel.GROUND_MIN_Y + 10) y = GamePanel.GROUND_MIN_Y + 10;
        if (y > GamePanel.GROUND_MAX_Y) y = GamePanel.GROUND_MAX_Y;
    }

    public void attackSoco() {
        if (attacking) return;
        attacking = true;
        hitboxActive = true;
        attackType = 1;
        attackDamage = 15;
        attackTimer = 12;
    }

    public void attackChute() {
        if (attacking) return;
        attacking = true;
        hitboxActive = true;
        attackType = 2;
        attackDamage = 25;
        attackTimer = 18;
    }

    public boolean activateFuria() {
        if (furia >= 100 && !isFuriaActive) {
            furia = 0;
            isFuriaActive = true;
            furiaTimer = 180; // 3 segundos de fúria
            attacking = true;
            hitboxActive = true;
            attackType = 3;
            attackDamage = 60;
            return true;
        }
        return false;
    }

    public void addFuria(int amount) {
        this.furia += amount;
        if (this.furia > 100) this.furia = 100;
    }

    @Override
    public void update() {
        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                attacking = false;
                hitboxActive = false;
            }
        }

        if (isFuriaActive) {
            furiaTimer--;
            if (furiaTimer <= 0) {
                isFuriaActive = false;
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        // Sombras do Personagem no Solo
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 20, y - 8, 40, 14);

        // Aura da Fúria do Dragão (Se ativa)
        if (isFuriaActive) {
            g2.setColor(new Color(255, 200, 0, 120));
            g2.fillOval(x - 30, y - height - 10, 60, height + 15);
        }

        // Sobretudo e Corpo de Thanatos Colt (Visual Cyberpunk / Sobretudo Escuro)
        g2.setColor(new Color(30, 35, 45)); // Sobretudo Azul-Escuro / Preto
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        // Cabeça
        g2.setColor(new Color(230, 190, 160)); // Pele
        g2.fillRect(drawX + 8, drawY, 20, 18);

        // Olho Biônico Neon (Vermelho)
        g2.setColor(Color.RED);
        int eyeX = facingRight ? drawX + 22 : drawX + 10;
        g2.fillRect(eyeX, drawY + 6, 4, 4);

        // Braço / Ataque Animation (Soco / Chute / Fúria)
        g2.setColor(isFuriaActive ? Color.YELLOW : Color.CYAN);
        if (attacking) {
            int armX = facingRight ? drawX + width : drawX - 25;
            if (attackType == 1) { // Soco
                g2.fillRect(armX, drawY + 22, 25, 10);
            } else if (attackType == 2) { // Chute
                g2.fillRect(armX, drawY + 40, 30, 12);
            } else if (attackType == 3) { // Fúria do Dragão (Onda de Corte)
                g2.setColor(new Color(255, 100, 0, 200));
                g2.fillArc(facingRight ? x : x - 70, drawY - 10, 70, height + 20, -60, 120);
            }
        }
    }

    public int getFuria() { return furia; }
    public boolean isFuriaPronta() { return furia >= 100; }
    public boolean isInvulnerable() { return isFuriaActive; }
}
