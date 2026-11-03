package Entities;

// Importa classes gráficas do pacote AWT, como Rectangle (para hitboxes de colisão) e Graphics2D (para renderização)
import java.awt.*;

/**
 * ============================================================================
 * CLASSE ABSTRATA BASE: ENTITY (PILAR DE ORIENTAÇÃO A OBJETOS: ABSTRAÇÃO E HERANÇA)
 * ============================================================================
 * Define a estrutura comum e o comportamento base compartilhado por todas as
 * entidades vivas do jogo (o Jogador Thanatos e os diferentes tipos de Inimigos).
 * 
 * Por ser abstrata ('abstract'), não pode ser instanciada diretamente com 'new Entity()'.
 * Ela serve de modelo para subclasses concretas (Player, BrutamontesEnemy, etc).
 * ============================================================================
 */
public abstract class Entity {
    
    // ------------------------------------------------------------------------
    // ATRIBUTOS DE POSICIONAMENTO E FÍSICA NO ESPAÇO 2.5D
    // ------------------------------------------------------------------------
    // 'protected' permite que as subclasses filhas acessem esses atributos diretamente.
    // As coordenadas (x, y) representam o ponto central inferior da entidade (os pés no chão).
    protected int x, y;          // Posição no plano cartesiano da tela (x horizontal, y vertical/profundidade)
    protected int width, height; // Largura e altura da caixa de colisão do corpo do personagem
    protected int hp, maxHp;     // Pontos de vida atuais e pontos de vida máximos
    protected int speed;         // Velocidade de deslocamento por frame (em pixels)
    protected boolean facingRight = true; // Direção para onde a entidade está olhando (true = direita, false = esquerda)
    protected boolean alive = true;       // Flag indicando se a entidade ainda está viva

    // ------------------------------------------------------------------------
    // ATRIBUTOS DE COMBATE E HITBOX
    // ------------------------------------------------------------------------
    protected boolean attacking = false;    // Indica se a entidade está no meio de uma animação de ataque
    protected boolean hitboxActive = false; // Indica se a caixa de dano (hitbox) está ativa para golpear naquele instante
    protected int attackTimer = 0;          // Contador de frames restantes para a animação do golpe terminar
    protected int attackDuration = 15;      // Duração total padrão do golpe em frames (~60 frames = 1 segundo)
    protected int attackDamage = 10;        // Quantidade de dano em pontos de vida que o ataque causa ao alvo

    /**
     * Construtor da classe base Entity.
     * Inicializa os atributos essenciais de qualquer personagem ao surgir no mundo.
     * 
     * @param x      Posição X inicial (eixo horizontal)
     * @param y      Posição Y inicial (eixo vertical / pés no chão)
     * @param width  Largura física do corpo
     * @param height Altura física do corpo
     * @param hp     Pontos de vida iniciais (também define a vida máxima)
     * @param speed  Velocidade de movimento em pixels por atualização
     */
    public Entity(int x, int y, int width, int height, int hp, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.maxHp = hp; // A vida máxima inicial é igual ao HP cheio
        this.speed = speed;
    }

    // ------------------------------------------------------------------------
    // MÉTODOS ABSTRATOS (POLIMORFISMO)
    // ------------------------------------------------------------------------
    // Toda classe filha que herdar de Entity É OBRIGADA a implementar estes dois métodos,
    // garantindo que cada personagem saiba como atualizar sua própria lógica e desenhar seu visual.

    /**
     * Atualização da lógica interna da entidade a cada ciclo do Game Loop.
     */
    public abstract void update();

    /**
     * Desenho visual da entidade na tela utilizando o contexto gráfico Graphics2D.
     * @param g2 Objeto gráfico 2D do Java para desenhar formas, cores e polígonos.
     */
    public abstract void render(Graphics2D g2);

    // ------------------------------------------------------------------------
    // MÉTODOS CONCRETOS DE COMPORTAMENTO
    // ------------------------------------------------------------------------

    /**
     * Aplica dano à entidade, reduzindo seus pontos de vida.
     * Caso o HP chegue a 0 ou menos, marca a entidade como morta (alive = false).
     * 
     * @param damage Quantidade de dano recebida.
     */
    public void takeDamage(int damage) {
        this.hp -= damage; // Subtrai a vida pelo dano recebido
        
        // Verifica se a vida acabou
        if (this.hp <= 0) {
            this.hp = 0;        // Garante que o HP não fique negativo
            this.alive = false; // Sinaliza que a entidade morreu
        }
    }

    /**
     * Retorna o retângulo de colisão corporal (Hurtbox) da entidade.
     * Como a coordenada 'x' representa o centro e 'y' representa os pés:
     * - O canto superior esquerdo do retângulo é (x - width/2, y - height).
     * 
     * @return Objeto Rectangle representando a área ocupada pelo corpo do personagem.
     */
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height, width, height);
    }

    /**
     * Retorna o retângulo da área de alcance do ataque (Hitbox).
     * Esse retângulo é projetado à frente do personagem, dependendo se ele está
     * olhando para a direita ou para a esquerda.
     * 
     * @return Objeto Rectangle representando a área atingida pelo ataque.
     */
    public Rectangle getAttackBounds() {
        int attackWidth = 40; // Largura do alcance do golpe em pixels
        
        // Se estiver virado para a direita, projeta o ataque à frente do lado direito do corpo;
        // Se estiver virado para a esquerda, projeta do lado esquerdo para trás.
        int attackX = facingRight ? x + (width / 2) : x - (width / 2) - attackWidth;
        
        // Cria a caixa de colisão do golpe na altura do tronco/peitoral
        return new Rectangle(attackX, y - height + 10, attackWidth, height - 10);
    }

    // ------------------------------------------------------------------------
    // GETTERS E SETTERS (CONCEITO DE ENCAPSULAMENTO)
    // Permitem a leitura e controle seguro dos atributos protegidos por outras classes.
    // ------------------------------------------------------------------------
    
    /** @return Coordenada horizontal X atual (centro do personagem). */
    public int getX() { return x; }

    /** @return Coordenada vertical Y atual (pés do personagem / profundidade). */
    public int getY() { return y; }

    /** @return Pontos de vida atuais. */
    public int getHp() { return hp; }

    /** @return Pontos de vida máximos. */
    public int getMaxHp() { return maxHp; }

    /** @return Verdadeiro se a entidade estiver viva; falso caso contrário. */
    public boolean isAlive() { return alive; }

    /** @return Verdadeiro se estiver em estado de animação de ataque. */
    public boolean isAttacking() { return attacking; }

    /** @return Verdadeiro se a hitbox de dano estiver ativa no frame atual. */
    public boolean hasHitboxActive() { return hitboxActive; }

    /** 
     * Ativa ou desativa a hitbox de dano (usado para evitar múltiplos hits no mesmo frame). 
     * @param active true para ativar, false para desativar.
     */
    public void setHitboxActive(boolean active) { this.hitboxActive = active; }

    /** @return Quantidade de dano causada pelo ataque atual. */
    public int getAttackDamage() { return attackDamage; }
}
