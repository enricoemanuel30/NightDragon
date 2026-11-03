package main.engine;

// Importa classes gráficas do AWT para cores, desenho de círculos e retângulos de colisão
import java.awt.*;

/**
 * ============================================================================
 * SISTEMA DE PROJÉTEIS (TIROS E BALAS DE ENERGIA DOS PISTOLEIROS)
 * ============================================================================
 * Representa qualquer projétil balístico em trânsito pela tela:
 * - Move-se horizontalmente com velocidade constante (vx).
 * - Possui detecção de colisão AABB (Axis-Aligned Bounding Box) através de getBounds().
 * - É desativado automaticamente ao ultrapassar as bordas da tela ou colidir com o jogador.
 * ============================================================================
 */
public class Projectile {

    // ------------------------------------------------------------------------
    // ATRIBUTOS DO PROJÉTIL
    // ------------------------------------------------------------------------
    private int x, y;          // Posição central do projétil no plano da tela
    private int vx;            // Velocidade vetorial horizontal (positivo = direita, negativo = esquerda)
    private int damage;        // Quantidade de dano infligido ao atingir o alvo
    private boolean active = true; // Flag que indica se o projétil ainda existe no mundo

    /**
     * Construtor do Projétil.
     * 
     * @param x      Posição X inicial de disparo
     * @param y      Posição Y inicial de disparo (altura da arma)
     * @param vx     Velocidade de deslocamento horizontal
     * @param damage Quantidade de dano ao colidir
     */
    public Projectile(int x, int y, int vx, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.damage = damage;
    }

    /**
     * Atualização da física do projétil a cada frame do Game Loop:
     * - Desloca a coordenada X com base na velocidade horizontal (vx).
     * - Verifica se o projétil saiu do campo de visão da tela (fora da largura 0 a WIDTH),
     *   marcando-o como inativo para que seja removido da memória.
     */
    public void update() {
        x += vx; // Aplica o movimento na direção do disparo

        // Se ultrapassar os limites esquerdo (0) ou direito (WIDTH), desativa
        if (x < 0 || x > GamePanel.WIDTH) {
            active = false;
        }
    }

    /**
     * Renderização visual da bala / projétil:
     * Desenha um núcleo amarelo brilhante envolto por uma borda alaranjada de energia/fogo.
     * 
     * @param g2 Contexto gráfico 2D
     */
    public void render(Graphics2D g2) {
        // Núcleo central amarelo do projétil
        g2.setColor(Color.YELLOW);
        g2.fillOval(x - 4, y - 4, 8, 8);

        // Halo / brilho exterior alaranjado
        g2.setColor(Color.ORANGE);
        g2.drawOval(x - 6, y - 6, 12, 12);
    }

    /**
     * Retorna a caixa de colisão retangular (Hitbox) do projétil para testes de colisão.
     * 
     * @return Rectangle delimitando a área de 8x8 pixels do projétil.
     */
    public Rectangle getBounds() {
        return new Rectangle(x - 4, y - 4, 8, 8);
    }

    // ------------------------------------------------------------------------
    // MÉTODOS DE ACESSO (GETTERS E SETTERS)
    // ------------------------------------------------------------------------

    /** @return true se o projétil estiver ativo; false se deve ser descartado. */
    public boolean isActive() { 
        return active; 
    }

    /** 
     * Altera o estado de atividade do projétil (ex: desativa após acertar o alvo). 
     * @param active novo estado de atividade
     */
    public void setActive(boolean active) { 
        this.active = active; 
    }

    /** @return O valor do dano que este projétil causa. */
    public int getDamage() { 
        return damage; 
    }
}
