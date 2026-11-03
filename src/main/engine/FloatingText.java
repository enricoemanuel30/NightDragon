package main.engine;

// Importa classes gráficas do AWT para manuseio de fontes tipográficas e cores
import java.awt.*;

/**
 * ============================================================================
 * SISTEMA DE TEXTO FLUTUANTE (FLOATING COMBAT TEXT / FEEDBACK VISUAL)
 * ============================================================================
 * Responsável por exibir mensagens dinâmicas e numerais na tela de combate:
 * - Indicadores numéricos de dano recebido ou infligido (ex: "-15", "-25")
 * - Notificações de bônus de combate (ex: "+25 FÚRIA")
 * - Avisos de novas fases/ondas (ex: "ONDA 2!")
 * 
 * Cada instância possui um tempo de vida (life) e sobe gradualmente na vertical,
 * criando uma animação fluida de desvanecimento.
 * ============================================================================
 */
public class FloatingText {

    // ------------------------------------------------------------------------
    // ATRIBUTOS DO TEXTO FLUTUANTE
    // ------------------------------------------------------------------------
    private String text;    // O texto literal ou número a ser desenhado na tela
    private int x, y;       // Posição no plano 2D onde o texto será renderizado
    private Color color;    // Cor do texto (ex: Vermelho para dano, Amarelo para fúria)
    private int life = 30;  // Duração de exibição em frames (30 frames = 0.5 segundo a 60 FPS)

    /**
     * Construtor da mensagem flutuante.
     * 
     * @param text  Mensagem de texto (ex: "-20")
     * @param x     Posição horizontal inicial
     * @param y     Posição vertical inicial
     * @param color Cor gráfica aplicada à fonte
     */
    public FloatingText(String text, int x, int y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    /**
     * Atualização do texto flutuante a cada frame:
     * - Desloca a coordenada Y para cima (-1 pixel por frame).
     * - Decrementa a vida restante da mensagem.
     */
    public void update() {
        y -= 1; // Sobe suavemente em direção ao topo da tela
        life--; // Reduz o tempo de vida restante
    }

    /**
     * Renderiza o texto na tela com estilo e tipografia destacados.
     * 
     * @param g2 Contexto gráfico 2D
     */
    public void render(Graphics2D g2) {
        g2.setColor(color); // Aplica a cor definida
        g2.setFont(new Font("SansSerif", Font.BOLD, 13)); // Define uma fonte legível em negrito
        g2.drawString(text, x, y); // Desenha a string nas coordenadas calculadas
    }

    /**
     * Verifica se o texto ainda deve permanecer visível e ativo na lista do jogo.
     * 
     * @return true se o tempo de vida for maior que 0; false quando expirar.
     */
    public boolean isActive() { 
        return life > 0; 
    }
}
