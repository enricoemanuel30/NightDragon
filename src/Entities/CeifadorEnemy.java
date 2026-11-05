package Entities;

import main.engine.Projectile;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * ============================================================================
 * INIMIGO CONCRETO: CEIFADOR (DEVIL REAPER - DEMÔNIO COM LANÇA/FOICE DE SANGUE)
 * ============================================================================
 * Subclasse de Enemy especializada em agilidade e corte em média distância.
 * Calibrado na mesma altura e tamanho que o Player (~85px de altura total).
 * ============================================================================
 */
public class CeifadorEnemy extends Enemy {

    // ------------------------------------------------------------------------
    // SISTEMA DE SPRITES E ANIMAÇÃO
    // ------------------------------------------------------------------------
    // Sprites originais têm ~145-149px de altura.
    // Escala 0.58 resulta em ~85px de altura renderizada, idêntica ao Thanatos!
    private static final double SCALE = 1.58;
    private int animFrame = 0;
    private int animTimer = 0;
    private boolean isMoving = false;

    public static class SpriteFrame {
        public BufferedImage image;
        public int anchorX, anchorY;

        public SpriteFrame(BufferedImage img, int ax, int ay) {
            this.image = img;
            this.anchorX = ax;
            this.anchorY = ay;
        }
    }

    private SpriteFrame[] walkFrames;
    private SpriteFrame[] attackFrames;
    private SpriteFrame[] thrustFrames;

    public CeifadorEnemy(int x, int y) {
        // x, y, width, height, hp, speed
        super(x, y, 40, 85, 75, 3);
        this.attackDamage = 20;
        loadSprites();
    }

    private void loadSprites() {
        try {
            walkFrames = new SpriteFrame[] {
                loadFrame("walk_0.png", 51, 149),
                loadFrame("walk_1.png", 54, 148),
                loadFrame("walk_2.png", 53, 146),
                loadFrame("walk_3.png", 54, 135)
            };

            attackFrames = new SpriteFrame[] {
                loadFrame("attack_0.png", 57, 132),
                loadFrame("attack_1.png", 49, 140),
                loadFrame("attack_2.png", 49, 143),
                loadFrame("attack_3.png", 57, 134)
            };

            thrustFrames = new SpriteFrame[] {
                loadFrame("thrust_0.png", 50, 143),
                loadFrame("thrust_1.png", 40, 144),
                loadFrame("thrust_2.png", 51, 144),
                loadFrame("thrust_3.png", 38, 144)
            };

            System.out.println("[CeifadorEnemy] Sprites do Ceifador carregados com sucesso!");
        } catch (Exception e) {
            System.err.println("[CeifadorEnemy] Erro ao carregar sprites: " + e.getMessage());
        }
    }

    private SpriteFrame loadFrame(String filename, int anchorX, int anchorY) {
        String[] paths = {
            "res/sprites/enemies/ceifador/" + filename,
            "src/res/sprites/enemies/ceifador/" + filename,
            "bin/res/sprites/enemies/ceifador/" + filename
        };

        BufferedImage img = null;
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (img == null) {
            try {
                InputStream is = getClass().getResourceAsStream("/res/sprites/enemies/ceifador/" + filename);
                if (is != null) {
                    img = ImageIO.read(is);
                }
            } catch (Exception ignored) {}
        }

        if (img != null) {
            return new SpriteFrame(img, anchorX, anchorY);
        }
        return null;
    }

    @Override
    public void updateAI(Player player, List<Projectile> projectiles) {
        if (!alive) return;

        facingRight = player.getX() > x;

        if (attacking) {
            isMoving = false;
            return;
        }

        isMoving = false;

        // 1. Aproximação horizontal rápida (X)
        if (Math.abs(player.getX() - x) > 45) {
            x += (player.getX() > x) ? speed : -speed;
            isMoving = true;
        }

        // 2. Alinhamento de profundidade (Y)
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? speed : -speed;
            isMoving = true;
        }

        // 3. Golpe de Foice / Lança Escarlate
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 55 && Math.abs(player.getY() - y) < 18) {
            attacking = true;
            hitboxActive = true;
            attackTimer = 20;
            attackCooldown = 55;
            animFrame = 0;
            animTimer = 0;
        }
    }

    @Override
    public void update() {
        super.update();

        animTimer++;
        if (attacking) {
            int speedPerFrame = 5;
            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (attackFrames != null && animFrame < attackFrames.length - 1) {
                    animFrame++;
                }
            }
        } else if (isMoving) {
            int speedPerFrame = 7;
            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (walkFrames != null && walkFrames.length > 0) {
                    animFrame = (animFrame + 1) % walkFrames.length;
                }
            }
        } else {
            animFrame = 0;
            animTimer = 0;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // 1. Sombra elíptica no chão
        g2.setColor(new Color(0, 0, 0, 105));
        g2.fillOval(x - 22, y - 7, 44, 14);

        // 2. Renderização do Sprite
        SpriteFrame frame = getCurrentSpriteFrame();
        if (frame != null && frame.image != null) {
            drawSpriteFrame(g2, frame, x, y, facingRight, SCALE);
        } else {
            renderFallback(g2);
        }

        // 3. Barra de Vida (HP)
        int drawX = x - width / 2;
        int drawY = y - height - 8;
        drawEnemyHP(g2, drawX, drawY);
    }

    private SpriteFrame getCurrentSpriteFrame() {
        if (attacking && attackFrames != null && attackFrames.length > 0) {
            return attackFrames[animFrame % attackFrames.length];
        } else if (walkFrames != null && walkFrames.length > 0) {
            return walkFrames[animFrame % walkFrames.length];
        }
        return null;
    }

    private void drawSpriteFrame(Graphics2D g2, SpriteFrame frame, int screenX, int screenY, boolean right, double scale) {
        int drawW = (int) Math.round(frame.image.getWidth() * scale);
        int drawH = (int) Math.round(frame.image.getHeight() * scale);
        int offsetX = (int) Math.round(frame.anchorX * scale);
        int offsetY = (int) Math.round(frame.anchorY * scale);

        int drawY = screenY - offsetY;

        if (right) {
            int drawX = screenX - offsetX;
            g2.drawImage(frame.image, drawX, drawY, drawW, drawH, null);
        } else {
            int drawX = screenX - (drawW - offsetX);
            g2.drawImage(frame.image, drawX + drawW, drawY, -drawW, drawH, null);
        }
    }

    private void renderFallback(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        g2.setColor(new Color(130, 20, 30));
        g2.fillRect(drawX + 5, drawY + 15, width - 10, height - 15);

        if (attacking) {
            g2.setColor(new Color(255, 30, 50));
            int bladeX = facingRight ? drawX + width : drawX - 25;
            g2.fillArc(bladeX, drawY + 15, 30, 45, 0, 180);
        }
    }

    private void drawEnemyHP(Graphics2D g2, int drawX, int drawY) {
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY, width, 5);

        g2.setColor(new Color(230, 40, 40));
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY, hpW, 5);

        g2.setColor(new Color(180, 180, 180));
        g2.drawRect(drawX, drawY, width, 5);
    }
}
