package main;

import main.engine.GamePanel;
import javax.swing.*;

/**
 * ============================================================================
 * JOGO 2D BEAT 'EM UP: THANATOS COLT - O DRAGÃO DA NOITE VS. DEVIL COWBOYS
 * ============================================================================
 * Ponto de entrada da aplicação.
 * Cria a janela principal e inicializa o GamePanel.
 * ============================================================================
 */
public class ThanatosBeatEmUp extends JFrame {

    public ThanatosBeatEmUp() {
        setTitle("Thanatos Colt: O Dragão da Noite - Beat 'em Up 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack(); // Ajusta a janela ao tamanho preferido do GamePanel (900x500)

        setLocationRelativeTo(null); // Centraliza na tela
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThanatosBeatEmUp game = new ThanatosBeatEmUp();
            game.setVisible(true);
        });
    }
}
