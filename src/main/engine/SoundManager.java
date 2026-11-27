package main.engine;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * GERENCIADOR DE ÁUDIO DO JOGO: SOUNDMANAGER
 * ============================================================================
 * Carrega e reproduz efeitos sonoros (.wav) utilizando a Java Sound API (javax.sound.sampled).
 * Permite reprodução simultânea e desacoplada em threads para não travar o Game Loop.
 * ============================================================================
 */
public class SoundManager {

    private static SoundManager instance;
    private final Map<String, byte[]> soundDataCache = new HashMap<>();

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private SoundManager() {
        // Pré-carrega os dados brutos de áudio na memória
        loadSoundToCache("hit", "res/audio/hit.wav");
        loadSoundToCache("coin", "res/audio/coin.wav");
        loadSoundToCache("rage", "res/audio/rage.wav");
        loadSoundToCache("roll", "res/audio/roll.wav");
    }

    private void loadSoundToCache(String key, String relativePath) {
        try {
            // Tenta carregar do sistema de arquivos
            String[] possiblePaths = {
                relativePath,
                "src/" + relativePath,
                "NightDragon/" + relativePath,
                "NightDragon/src/" + relativePath
            };

            File found = null;
            for (String p : possiblePaths) {
                File f = new File(p);
                if (f.exists()) {
                    found = f;
                    break;
                }
            }

            InputStream in = null;
            if (found != null) {
                in = new java.io.FileInputStream(found);
            } else {
                in = getClass().getResourceAsStream("/" + relativePath);
            }

            if (in != null) {
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] temp = new byte[4096];
                int read;
                while ((read = in.read(temp)) != -1) {
                    buffer.write(temp, 0, read);
                }
                in.close();
                soundDataCache.put(key, buffer.toByteArray());
                System.out.println("[SoundManager] Efeito sonoro carregado: " + key + " (" + buffer.size() + " bytes)");
            }
        } catch (Exception e) {
            System.err.println("[SoundManager] Aviso ao carregar som '" + key + "': " + e.getMessage());
        }
    }

    /**
     * Toca o efeito sonoro de forma assíncrona para não bloquear o Game Loop.
     */
    public void play(String key) {
        byte[] data = soundDataCache.get(key);
        if (data == null || data.length == 0) return;

        new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new java.io.ByteArrayInputStream(data));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                // Fecha a linha após a reprodução
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception ignored) {
                // Silencia falhas se o dispositivo de áudio do sistema estiver indisponível
            }
        }).start();
    }

    public void playHit() {
        play("hit");
    }

    public void playCoin() {
        play("coin");
    }

    public void playRage() {
        play("rage");
    }

    public void playRoll() {
        play("roll");
    }
}
