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
 * INIMIGO CONCRETO: PISTOLEIRO (DEVIL COWBOY GUNNER - ATIRADOR À DISTÂNCIA)
 * ============================================================================
 * Subclasse de Enemy especializada em combate com armas de fogo à distância.
 * Calibrado na mesma altura e tamanho que o Player (~85px de altura total).
 * ============================================================================
 */
public class PistoleiroEnemy extends Enemy {

    // ------------------------------------------------------------------------
    // SISTEMA DE SPRITES E ANIMAÇÃO
    // ------------------------------------------------------------------------
    // Sprite original tem 113-124px. Com scale 0.72, altura renderizada = ~85px
    // idêntica à do Thanatos Colt!
    private static final double SCALE = 1.25;
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

    private SpriteFrame[] idleFrames;
    private SpriteFrame[] walkFrames;
    private SpriteFrame[] shootFrames;

    public PistoleiroEnemy(int x, int y) {
        super(x, y, 38, 85, 55, 2);
        loadSprites();
    }

    private void loadSprites() {
        try {
            idleFrames = new SpriteFrame[] {
                loadFrame("idle_0.png", 29, 113)
            };

            walkFrames = new SpriteFrame[] {
                loadFrame("walk_0.png", 30, 123),
                loadFrame("walk_1.png", 22, 124),
                loadFrame("walk_2.png", 32, 122),
                loadFrame("walk_3.png", 24, 121),
                loadFrame("walk_4.png", 31, 123)
            };

            shootFrames = new SpriteFrame[] {
                loadFrame("shoot_0.png", 23, 112),
                loadFrame("shoot_1.png", 23, 112),
                loadFrame("crouch_shoot_2.png", 23, 113)
            };

            System.out.println("[PistoleiroEnemy] Sprites carregados com sucesso!");
        } catch (Exception e) {
            System.err.println("[PistoleiroEnemy] Erro ao carregar sprites: " + e.getMessage());
        }
    }

    private SpriteFrame loadFrame(String filename, int anchorX, int anchorY) {
        String[] paths = {
            "res/sprites/enemies/pistoleiro/" + filename,
            "src/res/sprites/enemies/pistoleiro/" + filename,
            "bin/res/sprites/enemies/pistoleiro/" + filename
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
                InputStream is = getClass().getResourceAsStream("/res/sprites/enemies/pistoleiro/" + filename);
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

        int distanceX = Math.abs(player.getX() - x);
        facingRight = player.getX() > x;

        if (attacking) {
            isMoving = false;
            return;
        }

        isMoving = false;

        if (distanceX > 260) {
            x += (player.getX() > x) ? speed : -speed;
            isMoving = true;
        } else if (distanceX < 150) {
            x += (player.getX() > x) ? -speed : speed;
            isMoving = true;
        }

        if (Math.abs(player.getY() - y) > 5) {
            y += (player.getY() > y) ? 1 : -1;
            isMoving = true;
        }

        if (attackCooldown <= 0 && distanceX <= 320 && Math.abs(player.getY() - y) < 22) {
            attacking = true;
            attackTimer = 24;
            attackCooldown = 85;
            animFrame = 0;
            animTimer = 0;

            int bulletSpeed = facingRight ? 8 : -8;
            int bulletX = facingRight ? x + 25 : x - 25;
            projectiles.add(new Projectile(bulletX, y - 50, bulletSpeed, 12));
        }
    }

    @Override
    public void update() {
        super.update();

        animTimer++;
        if (attacking) {
            int speedPerFrame = 8;
            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (shootFrames != null && animFrame < shootFrames.length - 1) {
                    animFrame++;
                }
            }
        } else if (isMoving) {
            int speedPerFrame = 6;
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
        g2.setColor(new Color(0, 0, 0, 105));
        g2.fillOval(x - 20, y - 7, 40, 14);

        SpriteFrame frame = getCurrentSpriteFrame();
        if (frame != null && frame.image != null) {
            drawSpriteFrame(g2, frame, x, y, facingRight, SCALE);
        } else {
            renderFallback(g2);
        }

        int drawX = x - width / 2;
        int drawY = y - height - 80;
        drawEnemyHP(g2, drawX, drawY);
    }

    private SpriteFrame getCurrentSpriteFrame() {
        if (attacking && shootFrames != null && shootFrames.length > 0) {
            return shootFrames[animFrame % shootFrames.length];
        } else if (isMoving && walkFrames != null && walkFrames.length > 0) {
            return walkFrames[animFrame % walkFrames.length];
        } else if (idleFrames != null && idleFrames.length > 0) {
            return idleFrames[0];
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

        g2.setColor(new Color(80, 50, 30));
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        g2.setColor(new Color(40, 30, 20));
        g2.fillRect(drawX - 4, drawY - 4, width + 8, 6);
        g2.fillRect(drawX + 6, drawY - 12, 20, 10);

        g2.setColor(new Color(200, 120, 40));
        int gunX = facingRight ? drawX + width : drawX - 15;
        g2.fillRect(gunX, drawY + 22, 15, 8);
    }

    private void drawEnemyHP(Graphics2D g2, int drawX, int drawY) {
        g2.setColor(Color.BLACK);
        g2.fillRect(drawX, drawY, width, 5);

        g2.setColor(new Color(230, 50, 50));
        int hpW = (int) ((hp / (double) maxHp) * width);
        g2.fillRect(drawX, drawY, hpW, 5);

        g2.setColor(new Color(180, 180, 180));
        g2.drawRect(drawX, drawY, width, 5);
    }
}
