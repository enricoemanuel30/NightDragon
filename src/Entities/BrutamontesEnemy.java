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
 * INIMIGO CONCRETO: BRUTAMONTES (DEVIL JESTER - PALHAÇO DEMONÍACO NO MONOCICLO)
 * ============================================================================
 * Subclasse de Enemy representando o Brutamontes do circo infernal:
 * - Alta resistência (140 HP)
 * - Avanço contínuo no monociclo jogando orbes de fogo infernal
 * - Calibrado na mesma altura e tamanho que o Player (~85px de altura total).
 * ============================================================================
 */
public class BrutamontesEnemy extends Enemy {

    // ------------------------------------------------------------------------
    // SISTEMA DE SPRITES E ANIMAÇÃO
    // ------------------------------------------------------------------------
    // Sprites originais têm ~155-159px de altura.
    // Escala 0.54 resulta em ~85px de altura renderizada, idêntica ao Thanatos!
    private static final double SCALE = 1.54;
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

    private SpriteFrame[] rideFrames;
    private SpriteFrame[] juggleFrames;
    private SpriteFrame[] slamFrames;

    public BrutamontesEnemy(int x, int y) {
        // x, y, width, height, hp, speed
        super(x, y, 42, 85, 140, 2);
        this.attackDamage = 25;
        loadSprites();
    }

    private void loadSprites() {
        try {
            rideFrames = new SpriteFrame[] {
                loadFrame("ride_0.png", 26, 156),
                loadFrame("ride_1.png", 36, 140),
                loadFrame("ride_2.png", 40, 138),
                loadFrame("ride_3.png", 38, 155),
                loadFrame("ride_4.png", 32, 159)
            };

            juggleFrames = new SpriteFrame[] {
                loadFrame("juggle_0.png", 29, 141),
                loadFrame("juggle_1.png", 45, 140),
                loadFrame("juggle_2.png", 45, 139),
                loadFrame("juggle_3.png", 36, 144),
                loadFrame("juggle_4.png", 37, 144)
            };

            slamFrames = new SpriteFrame[] {
                loadFrame("slam_0.png", 28, 181),
                loadFrame("slam_1.png", 42, 182),
                loadFrame("slam_2.png", 35, 158),
                loadFrame("slam_3.png", 28, 158),
                loadFrame("slam_4.png", 23, 158)
            };

            System.out.println("[BrutamontesEnemy] Sprites do Brutamontes Palhaço carregados com sucesso!");
        } catch (Exception e) {
            System.err.println("[BrutamontesEnemy] Erro ao carregar sprites: " + e.getMessage());
        }
    }

    private SpriteFrame loadFrame(String filename, int anchorX, int anchorY) {
        String[] paths = {
            "res/sprites/enemies/brutamontes/" + filename,
            "src/res/sprites/enemies/brutamontes/" + filename,
            "bin/res/sprites/enemies/brutamontes/" + filename
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
                InputStream is = getClass().getResourceAsStream("/res/sprites/enemies/brutamontes/" + filename);
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

        // 1. Perseguição no eixo horizontal (X)
        if (Math.abs(player.getX() - x) > 40) {
            x += (player.getX() > x) ? speed : -speed;
            isMoving = true;
        }

        // 2. Perseguição no eixo de profundidade (Y)
        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
            isMoving = true;
        }

        // 3. Ataque devastador de malabares / slam com orbes
        if (attackCooldown <= 0 && Math.abs(player.getX() - x) <= 50 && Math.abs(player.getY() - y) < 18) {
            attacking = true;
            hitboxActive = true;
            attackTimer = 25;
            attackCooldown = 75;
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
                if (slamFrames != null && animFrame < slamFrames.length - 1) {
                    animFrame++;
                }
            }
        } else if (isMoving) {
            int speedPerFrame = 6;
            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (rideFrames != null && rideFrames.length > 0) {
                    animFrame = (animFrame + 1) % rideFrames.length;
                }
            }
        } else {
            int speedPerFrame = 7;
            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (juggleFrames != null && juggleFrames.length > 0) {
                    animFrame = (animFrame + 1) % juggleFrames.length;
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // 1. Sombra circular do pneu do monociclo no solo
        g2.setColor(new Color(0, 0, 0, 115));
        g2.fillOval(x - 22, y - 7, 44, 14);

        // 2. Renderização do Sprite
        SpriteFrame frame = getCurrentSpriteFrame();
        if (frame != null && frame.image != null) {
            drawSpriteFrame(g2, frame, x, y, facingRight, SCALE);
        } else {
            renderFallback(g2);
        }

        // 3. Barra de HP
        int drawX = x - width / 2;
        int drawY = y - height - 8;
        drawEnemyHP(g2, drawX, drawY);
    }

    private SpriteFrame getCurrentSpriteFrame() {
        if (attacking && slamFrames != null && slamFrames.length > 0) {
            return slamFrames[animFrame % slamFrames.length];
        } else if (isMoving && rideFrames != null && rideFrames.length > 0) {
            return rideFrames[animFrame % rideFrames.length];
        } else if (juggleFrames != null && juggleFrames.length > 0) {
            return juggleFrames[animFrame % juggleFrames.length];
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

        g2.setColor(new Color(60, 65, 70));
        g2.fillRect(drawX, drawY, width, height);

        g2.setColor(Color.RED);
        g2.fillRect(drawX + 16, drawY + 20, 16, 16);
    }

    private void drawEnemyHP(Graphics2D g2, int drawX, int drawY) {
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY, width, 6);

        g2.setColor(new Color(230, 40, 40));
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY, hpW, 6);

        g2.setColor(new Color(180, 180, 180));
        g2.drawRect(drawX, drawY, width, 6);
    }
}
