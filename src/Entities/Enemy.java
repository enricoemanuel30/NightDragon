package Entities;

import main.engine.Projectile;
import java.util.List;

/**
 * ============================================================================
 * CLASSE ABSTRATA DE INIMIGOS (DEVIL COWBOYS)
 * ============================================================================
 * Herda de Entity. Adiciona cooldown de ataque e o método abstrato
 * updateAI() que cada inimigo concreto deve implementar.
 * ============================================================================
 */
public abstract class Enemy extends Entity {

    protected int attackCooldown = 0;
    protected int cooldownMax = 60; // 1 segundo entre ataques

    public Enemy(int x, int y, int width, int height, int hp, int speed) {
        super(x, y, width, height, hp, speed);
    }

    // IA Genérica de Perseguição no Plano 2.5D
    public abstract void updateAI(Player player, List<Projectile> projectiles);

    @Override
    public void update() {
        if (attackCooldown > 0) attackCooldown--;

        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                attacking = false;
                hitboxActive = false;
            }
        }
    }
}
