package Entities;

import java.awt.*;

/**
 * ============================================================================
 * FORMAS GEOMÉTRICAS E OBJETOS DO CENÁRIO (MINI PROJETO 01)
 * ============================================================================
 * Representa os objetos, obstáculos e moedas interativas do mundo do jogo:
 * - CÍRCULOS (Moedas / Fichas de Ouro do Saloon - Desafio Coletor de Moedas)
 * - RETÂNGULOS (Caixotes de Munição / Suprimentos)
 * - QUADRADOS (Barris de Carvalho / Obstáculos do Saloon)
 * 
 * Implementa renderização com formas geométricas primitivas do Java AWT,
 * física de limites e cálculo de colisão AABB (Rectangle).
 * ============================================================================
 */
public class WorldShape {

    public enum ShapeType {
        CIRCLE_COIN,      // Círculo: Moeda de ouro coletável (+10 pontos)
        RECTANGLE_CRATE,  // Retângulo: Caixote de suprimentos (+10 pontos)
        SQUARE_BARREL     // Quadrado: Barril de obstáculos (+10 pontos)
    }

    private int x;
    private int y;
    private int width;
    private int height;
    private ShapeType type;
    private boolean active = true;
    private int points = 10; // 10 pontos por colisão válida conforme requisito do professor
    private double bobTimer = 0; // Animação de flutuação suave

    public WorldShape(int x, int y, int width, int height, ShapeType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public static WorldShape createCoin(int x, int y) {
        return new WorldShape(x, y, 22, 22, ShapeType.CIRCLE_COIN);
    }

    public static WorldShape createCrate(int x, int y) {
        return new WorldShape(x, y, 40, 26, ShapeType.RECTANGLE_CRATE);
    }

    public static WorldShape createBarrel(int x, int y) {
        return new WorldShape(x, y, 28, 28, ShapeType.SQUARE_BARREL);
    }

    public void update() {
        bobTimer += 0.08;
    }

    public void render(Graphics2D g2) {
        if (!active) return;

        int drawX = x - width / 2;
        int drawY = y - height;

        // Sombra no solo
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(x - width / 2, y - 4, width, 8);

        switch (type) {
            case CIRCLE_COIN:
                // Desafio Coletor de Moedas: Círculos amarelos
                int floatOffset = (int) (Math.sin(bobTimer) * 4);
                int coinY = drawY + floatOffset;

                // Brilho exterior (Aura de Néon Dourado)
                g2.setColor(new Color(255, 220, 0, 90));
                g2.fillOval(drawX - 3, coinY - 3, width + 6, height + 6);

                // Moeda de Ouro sólida (Círculo Amarelo)
                g2.setColor(new Color(255, 204, 0));
                g2.fillOval(drawX, coinY, width, height);

                // Borda interna e detalhe de brilho
                g2.setColor(new Color(255, 245, 150));
                g2.fillOval(drawX + 4, coinY + 4, width - 8, height - 8);
                g2.setColor(new Color(190, 140, 0));
                g2.drawOval(drawX, coinY, width, height);
                break;

            case RECTANGLE_CRATE:
                // Retângulo: Caixote de Madeira do Saloon
                g2.setColor(new Color(110, 75, 45));
                g2.fillRect(drawX, drawY, width, height);
                
                // Bordas e travessas
                g2.setColor(new Color(70, 45, 25));
                g2.drawRect(drawX, drawY, width, height);
                g2.drawLine(drawX, drawY, drawX + width, drawY + height);
                g2.drawLine(drawX + width, drawY, drawX, drawY + height);

                // Néon cibernético nos cantos
                g2.setColor(new Color(0, 220, 255, 180));
                g2.fillRect(drawX + 2, drawY + 2, 4, 4);
                g2.fillRect(drawX + width - 6, drawY + 2, 4, 4);
                break;

            case SQUARE_BARREL:
                // Quadrado: Barril / Caixa de suprimentos
                g2.setColor(new Color(130, 85, 50));
                g2.fillRect(drawX, drawY, width, height);

                // Faixas de metal e reforço
                g2.setColor(new Color(180, 180, 190));
                g2.fillRect(drawX, drawY + 4, width, 3);
                g2.fillRect(drawX, drawY + height - 7, width, 3);

                g2.setColor(new Color(60, 40, 20));
                g2.drawRect(drawX, drawY, width, height);
                break;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public ShapeType getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getPoints() { return points; }
}
