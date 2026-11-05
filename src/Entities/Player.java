package Entities;

import main.engine.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;

/**
 * ============================================================================
 * PROTAGONISTA: THANATOS COLT (PLAYER)
 * ============================================================================
 * Herda de Entity. Gerencia movimento na arena 2.5D, sistema de combate
 * (Soco / Chute / Fúria do Dragão / Cambalhota nas 4 direções com Espaço)
 * e a barra de Fúria.
 * ============================================================================
 */
public class Player extends Entity {

    // ------------------------------------------------------------------------
    // ATRIBUTOS ESPECÍFICOS DO JOGADOR & FÚRIA (RAGE MODE)
    // ------------------------------------------------------------------------
    private int furia = 0;              // Barra de 0 a 100
    private boolean isFuriaActive = false;
    private int furiaTimer = 0;         // Timer restante do modo fúria
    public static final int FURIA_MAX_DURATION = 360; // 6 segundos a 60 FPS
    
    // Estado de explosão inicial de ativação da Fúria (scream/aura burst)
    private boolean isActivatingFuria = false;
    private int activationTimer = 0;
    private static final int ACTIVATION_DURATION = 30;

    // ------------------------------------------------------------------------
    // CAMBALHOTA / ROLL / ESQUIVA (ESPAÇO NAS 4 DIREÇÕES)
    // ------------------------------------------------------------------------
    private boolean isRolling = false;
    private int rollTimer = 0;
    private static final int ROLL_DURATION = 24; // 24 frames (~0.4s)
    private int rollDx = 0;
    private int rollDy = 0;
    private static final int ROLL_SPEED = 7;     // Velocidade da cambalhota
    private int rollCooldown = 0;

    private int attackType = 1;         // 1 = Soco, 2 = Chute, 3 = Ataque Especial / Fúria
    private boolean isMoving = false;

    // ------------------------------------------------------------------------
    // SISTEMA DE SPRITES E ANIMAÇÃO
    // ------------------------------------------------------------------------
    // Escala padrão calibrada para ~85-90px de altura
    private static final double SCALE = 0.80;
    private int animFrame = 0;
    private int animTimer = 0;

    public static class SpriteFrame {
        public BufferedImage image;
        public int anchorX, anchorY; // Ponto de ancoragem (centro dos pés na base)

        public SpriteFrame(BufferedImage img, int ax, int ay) {
            this.image = img;
            this.anchorX = ax;
            this.anchorY = ay;
        }
    }

    // Frames Normais (base original do Thanatos)
    private SpriteFrame[] idleFrames;
    private SpriteFrame[] walkFrames;
    private SpriteFrame[] punchFrames;
    private SpriteFrame[] kickFrames;
    private SpriteFrame[] gunFrames;
    private SpriteFrame[] rollFrames;           // Nova animação de Cambalhota (12 frames)

    // Frames do MODO FÚRIA (THANATOS-BRUTO - chamas vermelhas / aura)
    private SpriteFrame[] rageActivationFrames; // Grito e explosão de aura (Row 0)
    private SpriteFrame[] rageIdleFrames;       // Parado com aura flamejante (Row 0)
    private SpriteFrame[] rageRunFrames;        // Corrida / investida com chamas (Row 2)
    private SpriteFrame[] ragePunchFrames;      // Golpes com garras de sangue (Row 1)
    private SpriteFrame[] rageKickFrames;       // Chutes com rastro de plasma vermelho (Row 1)
    private SpriteFrame[] rageSpecialFrames;    // Erupção de chamas no solo (Row 2)
    private BufferedImage ragePortrait;         // Retrato do Thanatos com charuto e olhos vermelhos

    /**
     * Construtor do Jogador Thanatos Colt.
     * 
     * @param x Posição X inicial
     * @param y Posição Y inicial
     */
    public Player(int x, int y) {
        // x, y, width, height, hp, speed
        super(x, y, 42, 85, 100, 4);
        loadAnimations();
    }

    /**
     * Carrega as animações normais, de cambalhota e do Modo Fúria.
     */
    private void loadAnimations() {
        try {
            // --- FRAMES NORMAIS ---
            idleFrames = new SpriteFrame[] {
                loadFrame("clean_frames/idle_0.png", 42, 176),
                loadFrame("clean_frames/idle_1.png", 40, 175),
                loadFrame("clean_frames/idle_2.png", 34, 175),
                loadFrame("clean_frames/idle_3.png", 39, 176),
                loadFrame("clean_frames/idle_4.png", 39, 175),
                loadFrame("clean_frames/idle_5.png", 26, 175)
            };

            walkFrames = new SpriteFrame[] {
                loadFrame("clean_frames/walk_0.png", 51, 170),
                loadFrame("clean_frames/walk_1.png", 70, 172),
                loadFrame("clean_frames/walk_2.png", 54, 172),
                loadFrame("clean_frames/walk_3.png", 42, 171),
                loadFrame("clean_frames/walk_4.png", 48, 173)
            };

            punchFrames = new SpriteFrame[] {
                loadFrame("clean_frames/punch_0.png", 46, 138),
                loadFrame("clean_frames/punch_1.png", 60, 135),
                loadFrame("clean_frames/punch_2.png", 53, 133),
                loadFrame("clean_frames/punch_3.png", 47, 134),
                loadFrame("clean_frames/punch_4.png", 45, 150)
            };

            kickFrames = new SpriteFrame[] {
                loadFrame("clean_frames/kick_0.png", 30, 147),
                loadFrame("clean_frames/kick_1.png", 31, 157),
                loadFrame("clean_frames/kick_2.png", 34, 158),
                loadFrame("clean_frames/kick_3.png", 54, 115)
            };

            gunFrames = new SpriteFrame[] {
                loadFrame("clean_frames/gun_0.png", 26, 145),
                loadFrame("clean_frames/gun_1.png", 40, 143),
                loadFrame("clean_frames/gun_2.png", 36, 146),
                loadFrame("clean_frames/gun_3.png", 38, 146),
                loadFrame("clean_frames/gun_4.png", 30, 148)
            };

            // --- CAMBALHOTA / ROLL FRAMES (12 frames) ---
            rollFrames = new SpriteFrame[] {
                loadFrame("roll_frames/roll_0.png", 41, 156),
                loadFrame("roll_frames/roll_1.png", 34, 120),
                loadFrame("roll_frames/roll_2.png", 73, 135),
                loadFrame("roll_frames/roll_3.png", 55, 140),
                loadFrame("roll_frames/roll_4.png", 47, 155),
                loadFrame("roll_frames/roll_5.png", 64, 140),
                loadFrame("roll_frames/roll_6.png", 56, 152),
                loadFrame("roll_frames/roll_7.png", 89, 119),
                loadFrame("roll_frames/roll_8.png", 42, 117),
                loadFrame("roll_frames/roll_9.png", 40, 135),
                loadFrame("roll_frames/roll_10.png", 27, 154),
                loadFrame("roll_frames/roll_11.png", 26, 158)
            };

            // --- FRAMES DO MODO FÚRIA (THANATOS-BRUTO) ---
            rageActivationFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_act_0.png", 71, 227),
                loadFrame("rage_frames/rage_act_1.png", 68, 225),
                loadFrame("rage_frames/rage_act_2.png", 61, 231),
                loadFrame("rage_frames/rage_act_3.png", 76, 253),
                loadFrame("rage_frames/rage_act_4.png", 115, 188)
            };

            rageIdleFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_idle_0.png", 71, 227),
                loadFrame("rage_frames/rage_idle_1.png", 68, 225),
                loadFrame("rage_frames/rage_idle_2.png", 61, 231),
                loadFrame("rage_frames/rage_idle_3.png", 68, 225)
            };

            rageRunFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_run_0.png", 65, 145),
                loadFrame("rage_frames/rage_run_1.png", 75, 161)
            };

            ragePunchFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_punch_0.png", 86, 165),
                loadFrame("rage_frames/rage_punch_1.png", 70, 155),
                loadFrame("rage_frames/rage_punch_2.png", 50, 156)
            };

            rageKickFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_kick_0.png", 55, 143),
                loadFrame("rage_frames/rage_kick_1.png", 65, 161),
                loadFrame("rage_frames/rage_kick_2.png", 102, 168)
            };

            rageSpecialFrames = new SpriteFrame[] {
                loadFrame("rage_frames/rage_special_0.png", 125, 183),
                loadFrame("rage_frames/rage_special_1.png", 121, 193)
            };

            SpriteFrame portFrame = loadFrame("rage_frames/rage_portrait_0.png", 79, 103);
            if (portFrame != null) {
                ragePortrait = portFrame.image;
            }

            System.out.println("[Player] Animações completas (normal, roll, fúria) carregadas!");
        } catch (Exception e) {
            System.err.println("[Player] Erro carregando sprites: " + e.getMessage());
        }
    }

    private SpriteFrame loadFrame(String relativePath, int anchorX, int anchorY) {
        String[] paths = {
            "res/sprites/player/" + relativePath,
            "src/res/sprites/player/" + relativePath,
            "bin/res/sprites/player/" + relativePath
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
                InputStream is = getClass().getResourceAsStream("/res/sprites/player/" + relativePath);
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

    /**
     * Inicia a Cambalhota / Esquiva (Tecla Espaço):
     * Executa nas 4 direções (W, S, A, D) ou na direção que o personagem está olhando.
     */
    public void startRoll(int inputDx, int inputDy) {
        if (isRolling || isActivatingFuria || rollCooldown > 0) return;

        isRolling = true;
        rollTimer = ROLL_DURATION;
        rollCooldown = 15; // Cooldown leve após terminar
        attacking = false;
        hitboxActive = false;
        animFrame = 0;
        animTimer = 0;

        // Se houver direção indicada, usa-a. Caso contrário, rola para frente
        if (inputDx == 0 && inputDy == 0) {
            rollDx = facingRight ? 1 : -1;
            rollDy = 0;
        } else {
            rollDx = inputDx;
            rollDy = inputDy;
            if (rollDx > 0) facingRight = true;
            else if (rollDx < 0) facingRight = false;
        }
    }

    /**
     * Processa a movimentação do jogador baseada nas teclas direcionais.
     */
    public void move(int dx, int dy) {
        this.isMoving = (dx != 0 || dy != 0);

        if (isActivatingFuria) return;

        // Durante a cambalhota, o deslocamento é governado pela física do roll
        if (isRolling) {
            x += rollDx * ROLL_SPEED;
            y += rollDy * (int)(ROLL_SPEED * 0.7); // Eixo Y proporcional à perspectiva 2.5D
            clampBounds();
            return;
        }

        if (attacking && !isFuriaActive) return;

        int currentSpeed = isFuriaActive ? (int)(speed * 1.4) : speed;

        if (dx > 0) { 
            x += currentSpeed; 
            facingRight = true; 
        }
        if (dx < 0) { 
            x -= currentSpeed; 
            facingRight = false; 
        }

        if (dy > 0) { 
            y += currentSpeed; 
        }
        if (dy < 0) { 
            y -= currentSpeed; 
        }

        clampBounds();
    }

    private void clampBounds() {
        if (x < 60) x = 60;
        if (x > GamePanel.WIDTH - 60) x = GamePanel.WIDTH - 60;
        if (y < GamePanel.GROUND_MIN_Y + 10) y = GamePanel.GROUND_MIN_Y + 10;
        if (y > GamePanel.GROUND_MAX_Y) y = GamePanel.GROUND_MAX_Y;
    }

    /** Soco (Tecla J) */
    public void attackSoco() {
        if (attacking || isActivatingFuria || isRolling) return;
        attacking = true;
        hitboxActive = true;
        attackType = 1;
        attackDamage = isFuriaActive ? 35 : 20;
        attackTimer = 16;
        animFrame = 0;
        animTimer = 0;
    }

    /** Chute (Tecla K) */
    public void attackChute() {
        if (attacking || isActivatingFuria || isRolling) return;
        attacking = true;
        hitboxActive = true;
        attackType = 2;
        attackDamage = isFuriaActive ? 50 : 30;
        attackTimer = 20;
        animFrame = 0;
        animTimer = 0;
    }

    /** Fúria do Dragão (Tecla L) */
    public boolean activateFuria() {
        if (furia >= 100 && !isFuriaActive && !isActivatingFuria && !isRolling) {
            furia = 100;
            isFuriaActive = true;
            isActivatingFuria = true;
            activationTimer = ACTIVATION_DURATION;
            furiaTimer = FURIA_MAX_DURATION;
            animFrame = 0;
            animTimer = 0;
            return true;
        }
        return false;
    }

    public void heal(int amount) {
        this.hp += amount;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    public void addFuria(int amount) {
        if (isFuriaActive) return;
        this.furia += amount;
        if (this.furia > 100) this.furia = 100;
    }

    @Override
    public void update() {
        if (rollCooldown > 0) rollCooldown--;

        // 1. Atualização do Roll / Cambalhota
        if (isRolling) {
            rollTimer--;
            animTimer++;
            int speedPerFrame = ROLL_DURATION / (rollFrames != null ? rollFrames.length : 12);
            if (speedPerFrame < 1) speedPerFrame = 1;

            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (rollFrames != null && animFrame < rollFrames.length - 1) {
                    animFrame++;
                }
            }

            if (rollTimer <= 0) {
                isRolling = false;
                animFrame = 0;
                animTimer = 0;
            }
            return;
        }

        // 2. Processo de ativação da Fúria
        if (isActivatingFuria) {
            activationTimer--;
            animTimer++;
            int speedPerFrame = ACTIVATION_DURATION / (rageActivationFrames != null ? rageActivationFrames.length : 5);
            if (speedPerFrame < 1) speedPerFrame = 1;

            if (animTimer >= speedPerFrame) {
                animTimer = 0;
                if (rageActivationFrames != null && animFrame < rageActivationFrames.length - 1) {
                    animFrame++;
                }
            }

            if (activationTimer <= 0) {
                isActivatingFuria = false;
                animFrame = 0;
                animTimer = 0;
            }
            return;
        }

        // 3. Consumo da Fúria
        if (isFuriaActive) {
            furiaTimer--;
            furia = (int) Math.round((furiaTimer / (double) FURIA_MAX_DURATION) * 100);
            if (furiaTimer <= 0) {
                furia = 0;
                isFuriaActive = false;
            }
        }

        // 4. Ataques
        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                attacking = false;
                hitboxActive = false;
            }
        }

        // 5. Animações
        animTimer++;
        if (attacking) {
            SpriteFrame[] cur = getCurrentAttackFrames();
            int spd = 4;
            if (animTimer >= spd) {
                animTimer = 0;
                if (cur != null && cur.length > 0) {
                    animFrame = (animFrame + 1) % cur.length;
                }
            }
        } else if (isMoving) {
            int spd = isFuriaActive ? 5 : 6;
            if (animTimer >= spd) {
                animTimer = 0;
                SpriteFrame[] cur = isFuriaActive ? rageRunFrames : walkFrames;
                if (cur != null && cur.length > 0) {
                    animFrame = (animFrame + 1) % cur.length;
                }
            }
        } else {
            int spd = isFuriaActive ? 6 : 8;
            if (animTimer >= spd) {
                animTimer = 0;
                SpriteFrame[] cur = isFuriaActive ? rageIdleFrames : idleFrames;
                if (cur != null && cur.length > 0) {
                    animFrame = (animFrame + 1) % cur.length;
                }
            }
        }
    }

    private SpriteFrame[] getCurrentAttackFrames() {
        if (isFuriaActive) {
            switch (attackType) {
                case 1: return ragePunchFrames;
                case 2: return rageKickFrames;
                case 3: return rageSpecialFrames;
                default: return ragePunchFrames;
            }
        } else {
            switch (attackType) {
                case 1: return punchFrames;
                case 2: return kickFrames;
                case 3: return gunFrames;
                default: return punchFrames;
            }
        }
    }

    private SpriteFrame[] getCurrentAnimationFrames() {
        if (isRolling) {
            return rollFrames;
        } else if (isActivatingFuria) {
            return rageActivationFrames;
        } else if (attacking) {
            return getCurrentAttackFrames();
        } else if (isMoving) {
            return isFuriaActive ? rageRunFrames : walkFrames;
        } else {
            return isFuriaActive ? rageIdleFrames : idleFrames;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        SpriteFrame[] frames = getCurrentAnimationFrames();
        if (frames == null || frames.length == 0 || frames[0] == null) {
            renderFallback(g2);
            return;
        }

        int frameIdx = animFrame % frames.length;
        SpriteFrame frame = frames[frameIdx];
        if (frame == null || frame.image == null) {
            renderFallback(g2);
            return;
        }

        // Sombra elíptica no chão (pés em x, y)
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillOval(x - 24, y - 8, 48, 16);

        // Rastro de poeira / fumaça durante o roll
        if (isRolling) {
            g2.setColor(new Color(200, 170, 140, 80));
            g2.fillOval(x - 20 - rollDx * 15, y - 6, 30, 12);
        }

        // Aura flamejante no modo fúria
        if (isFuriaActive) {
            g2.setColor(new Color(255, 30, 0, 70));
            g2.fillOval(x - 38, y - 12, 76, 24);

            if (isActivatingFuria) {
                g2.setColor(new Color(255, 50, 0, 120));
                int shockRadius = (ACTIVATION_DURATION - activationTimer) * 4;
                g2.drawOval(x - shockRadius, y - 10 - shockRadius / 2, shockRadius * 2, shockRadius);
            }
        }

        drawSpriteFrame(g2, frame, x, y, facingRight, SCALE);
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

        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 20, y - 8, 40, 14);

        if (isFuriaActive) {
            g2.setColor(new Color(255, 50, 0, 130));
            g2.fillOval(x - 30, y - height - 10, 60, height + 15);
        }

        g2.setColor(new Color(30, 35, 45));
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        g2.setColor(new Color(230, 190, 160));
        g2.fillRect(drawX + 8, drawY, 20, 18);

        g2.setColor(Color.RED);
        int eyeX = facingRight ? drawX + 22 : drawX + 10;
        g2.fillRect(eyeX, drawY + 6, 4, 4);

        if (attacking) {
            g2.setColor(isFuriaActive ? Color.RED : Color.CYAN);
            int armX = facingRight ? drawX + width : drawX - 25;
            g2.fillRect(armX, drawY + 22, 25, 10);
        }
    }

    public int getFuria() { return furia; }
    public boolean isFuriaPronta() { return furia >= 100; }
    public boolean isFuriaActive() { return isFuriaActive; }
    public boolean isActivatingFuria() { return isActivatingFuria; }
    public boolean isRolling() { return isRolling; }
    public double getFuriaPercent() { return (double) furiaTimer / FURIA_MAX_DURATION; }
    public BufferedImage getRagePortrait() { return ragePortrait; }
    // Durante a cambalhota ou modo fúria, o jogador fica invulnerável (i-frames na esquiva)
    public boolean isInvulnerable() { return isFuriaActive || isRolling; }
}
