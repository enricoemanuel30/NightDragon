package main;

// Importa a classe do painel do jogo contendo o loop, entidades e renderização
import main.engine.GamePanel;

// Importa as classes da biblioteca gráfica Swing (JFrame, SwingUtilities, etc.)
import javax.swing.*;

/**
 * ============================================================================
 * JOGO 2D BEAT 'EM UP: THANATOS COLT - O DRAGÃO DA NOITE VS. DEVIL COWBOYS
 * ============================================================================
 * Classe principal / Ponto de entrada da aplicação.
 * Herda de JFrame (javax.swing.JFrame) para atuar como a janela principal do jogo.
 * ============================================================================
 */
public class ThanatosBeatEmUp extends JFrame {

    /**
     * Construtor da janela principal.
     * Configura propriedades da janela e acopla o painel de jogo (GamePanel).
     */
    public ThanatosBeatEmUp() {
        // Define o título exibido na barra superior da janela
        setTitle("Thanatos Colt: O Dragão da Noite - Beat 'em Up 2D");

        // Define a operação padrão ao fechar: encerra totalmente o processo Java (evita processos órfãos em segundo plano)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bloqueia o redimensionamento da janela para não distorcer a resolução nem quebrar os limites dos eixos X e Y
        setResizable(false);

        // Instancia o GamePanel, onde roda o Game Loop (Thread), física, colisão e desenho
        GamePanel gamePanel = new GamePanel();

        // Adiciona o GamePanel à área de conteúdo da janela (JFrame)
        add(gamePanel);

        // Ajusta automaticamente o tamanho da janela (JFrame) para envolver exatamente as dimensões preferidas do GamePanel (900x500)
        pack();

        // Centraliza a janela exatamente no meio da tela do monitor (passar null usa a tela como referência)
        setLocationRelativeTo(null);
    }

    /**
     * Método principal (Main) - Ponto de partida de execução da Máquina Virtual Java (JVM).
     * @param args Argumentos de linha de comando (não utilizados no jogo).
     */
    public static void main(String[] args) {
        // Garante que a interface gráfica do Swing seja criada e manipulada dentro da EDT (Event Dispatch Thread),
        // que é a Thread segura do Java recomendada para manipulação de componentes gráficos.
        SwingUtilities.invokeLater(() -> {
            // Cria a instância da nossa janela
            ThanatosBeatEmUp game = new ThanatosBeatEmUp();

            // Torna a janela visível para o usuário
            game.setVisible(true);
        });
    }
}
