package Entities;

import java.awt.*;

/**
 * ============================================================================
 * CLASSE ABSTRATA BASE: ENTITY (CONCEITO DE HERANÇA E POLIMORFISMO)
 * ============================================================================
 */
public abstract class Entity {
    protected int x, y;
    protected int width, height;
    protected int hp, maxHp;
    protected int speed;
    protected boolean facingRight = true;
    protected boolean alive = true;

    // Estado de Combate
    protected boolean attacking = false;
    protected boolean hitboxActive = false;
    protected int attackTimer = 0;
    protected int attackDuration = 15; // frames de animação
    protected int attackDamage = 10;

    public Entity(int x, int y, int width, int height, int hp, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
    }

    public abstract void update();
    public abstract void render(Graphics2D g2);

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height, width, height);
    }

    public Rectangle getAttackBounds() {
        int attackWidth = 40;
        int attackX = facingRight ? x + (width / 2) : x - (width / 2) - attackWidth;
        return new Rectangle(attackX, y - height + 10, attackWidth, height - 10);
    }

    // Getters & Setters (Encapsulamento)
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isAlive() { return alive; }
    public boolean isAttacking() { return attacking; }
    public boolean hasHitboxActive() { return hitboxActive; }
    public void setHitboxActive(boolean active) { this.hitboxActive = active; }
    public int getAttackDamage() { return attackDamage; }
}
