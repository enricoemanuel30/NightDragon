package Entities;

// Importa a classe Projectile para que inimigos que atiram (ex: Pistoleiro) possam gerar tiros
import main.engine.Projectile;

// Importa a interface List para receber a lista de projéteis da arena
import java.util.List;

/**
 * ============================================================================
 * CLASSE ABSTRATA INTERMEDIÁRIA: ENEMY (INIMIGOS - FACÇÃO DEVIL COWBOYS)
 * ============================================================================
 * Especializa a classe Entity para todos os inimigos do jogo.
 * Herda todos os atributos básicos (vida, posição, colisão) e introduz
 * o sistema de inteligência artificial (IA) e o tempo de recarga (cooldown) de ataque.
 * ============================================================================
 */
public abstract class Enemy extends Entity {

    // ------------------------------------------------------------------------
    // ATRIBUTOS DE CADÊNCIA E TEMPORIZAÇÃO DE ATAQUE
    // ------------------------------------------------------------------------
    protected int attackCooldown = 0; // Contador regressivo de frames até o próximo golpe permitido
    protected int cooldownMax = 60;   // Tempo padrão de recarga: 60 frames = aproximadamente 1 segundo em 60 FPS

    /**
     * Construtor da classe base de inimigos.
     * Repassa os parâmetros essenciais para o construtor da superclasse Entity via 'super(...)'.
     * 
     * @param x      Posição X inicial na arena
     * @param y      Posição Y inicial na arena
     * @param width  Largura do inimigo
     * @param height Altura do inimigo
     * @param hp     Pontos de vida
     * @param speed  Velocidade de locomoção
     */
    public Enemy(int x, int y, int width, int height, int hp, int speed) {
        super(x, y, width, height, hp, speed);
    }

    /**
     * MÉTODO ABSTRATO DE INTELIGÊNCIA ARTIFICIAL (IA)
     * Deve ser implementado por cada classe concreta de inimigo (Pistoleiro, Brutamontes, Ceifador).
     * Cada inimigo decide se persegue, recua, golpeia corpo a corpo ou dispara à distância.
     * 
     * @param player      Referência ao protagonista Thanatos Colt (para calcular distâncias e miras)
     * @param projectiles Lista compartilhada de projéteis ativos no GamePanel
     */
    public abstract void updateAI(Player player, List<Projectile> projectiles);

    /**
     * Atualiza o estado temporal do inimigo a cada frame.
     * Sobrescreve o método update() da classe Entity:
     * 1. Reduz o cooldown de ataque até que ele possa atacar novamente.
     * 2. Caso esteja atacando, reduz o temporizador da animação e desativa a hitbox ao terminar.
     */
    @Override
    public void update() {
        // Reduz o tempo de espera entre ataques se ainda estiver em recarga
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Se o inimigo estiver desferindo um ataque no momento
        if (attacking) {
            attackTimer--; // Decrementa a duração do golpe
            
            // Quando a duração da animação do ataque chega a zero
            if (attackTimer <= 0) {
                attacking = false;    // Encerra a animação de ataque
                hitboxActive = false; // Desativa a caixa de dano (não causa mais dano residual)
            }
        }
    }
}
