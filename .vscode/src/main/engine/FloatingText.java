package main.engine;

import java.awt.*;

/**
 * ============================================================================
 * DANO E STATUS FLUTUANTES (FEEDBACK VISUAL NA TELA)
 * ============================================================================
 * Exibe texto colorido que sobe durante 0.5 segundo (30 frames)
 * para indicar dano, ganho de Fúria ou outros eventos de jogo.
 * ============================================================================
 */
public class FloatingText {
    private String text;
    private int x, y;
    private Color color;
    private int life = 30; // 0.5 segundo de exibição

    public FloatingText(String text, int x, int y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void update() {
        y -= 1; // Flutua para cima
        life--;
    }

    public void render(Graphics2D g2) {
        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(text, x, y);
    }

    public boolean isActive() { return life > 0; }
}
