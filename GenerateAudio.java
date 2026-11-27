import java.io.*;

public class GenerateAudio {

    public static void main(String[] args) {
        String[] dirs = {
            "res/audio",
            "src/res/audio",
            "../res/audio"
        };

        for (String d : dirs) {
            new File(d).mkdirs();
        }

        generateWav(dirs, "hit.wav", createHitSound(22050));
        generateWav(dirs, "coin.wav", createCoinSound(22050));
        generateWav(dirs, "rage.wav", createRageSound(22050));
        generateWav(dirs, "roll.wav", createRollSound(22050));

        System.out.println("Audio files generated successfully!");
    }

    private static void generateWav(String[] dirs, String filename, byte[] pcmData) {
        int sampleRate = 22050;
        int channels = 1;
        int bitsPerSample = 16;
        int byteRate = sampleRate * channels * (bitsPerSample / 8);
        int blockAlign = channels * (bitsPerSample / 8);
        int dataSize = pcmData.length;
        int chunkSize = 36 + dataSize;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // RIFF header
            baos.write(new byte[]{'R', 'I', 'F', 'F'});
            writeLEInt(baos, chunkSize);
            baos.write(new byte[]{'W', 'A', 'V', 'E'});

            // fmt subchunk
            baos.write(new byte[]{'f', 'm', 't', ' '});
            writeLEInt(baos, 16); // Subchunk1Size
            writeLEShort(baos, (short) 1); // AudioFormat PCM
            writeLEShort(baos, (short) channels);
            writeLEInt(baos, sampleRate);
            writeLEInt(baos, byteRate);
            writeLEShort(baos, (short) blockAlign);
            writeLEShort(baos, (short) bitsPerSample);

            // data subchunk
            baos.write(new byte[]{'d', 'a', 't', 'a'});
            writeLEInt(baos, dataSize);
            baos.write(pcmData);

            byte[] wavBytes = baos.toByteArray();
            for (String d : dirs) {
                File target = new File(d, filename);
                FileOutputStream fos = new FileOutputStream(target);
                fos.write(wavBytes);
                fos.close();
                System.out.println("Saved: " + target.getPath() + " (" + wavBytes.length + " bytes)");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLEInt(OutputStream out, int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    private static void writeLEShort(OutputStream out, short value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    // Impact punch sound: fast pitch decay with noise
    private static byte[] createHitSound(int sampleRate) {
        int length = (int) (sampleRate * 0.18); // 180ms
        byte[] data = new byte[length * 2];
        double freq = 220.0;
        java.util.Random rnd = new java.util.Random(42);

        for (int i = 0; i < length; i++) {
            double t = (double) i / sampleRate;
            double env = Math.exp(-t * 22.0); // fast decay
            double currentFreq = freq * Math.exp(-t * 15.0);
            double wave = Math.sin(2.0 * Math.PI * currentFreq * t);
            double noise = (rnd.nextDouble() * 2.0 - 1.0) * 0.4;
            double sample = (wave * 0.7 + noise * 0.3) * env;
            short s = (short) Math.max(-32767, Math.min(32767, (int) (sample * 28000)));
            data[i * 2] = (byte) (s & 0xFF);
            data[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return data;
    }

    // Coin chime: two crisp high-frequency bell tones (987 Hz -> 1318 Hz)
    private static byte[] createCoinSound(int sampleRate) {
        int length = (int) (sampleRate * 0.28); // 280ms
        byte[] data = new byte[length * 2];
        int split = length / 2;

        for (int i = 0; i < length; i++) {
            double t = (double) i / sampleRate;
            double f = (i < split) ? 987.77 : 1318.51; // B5 -> E6
            double env = (i < split) ? (1.0 - (double) i / split * 0.4) : Math.exp(-(t - (double) split / sampleRate) * 12.0);
            double wave = Math.sin(2.0 * Math.PI * f * t) + 0.25 * Math.sin(4.0 * Math.PI * f * t);
            double sample = wave * env * 0.8;
            short s = (short) Math.max(-32767, Math.min(32767, (int) (sample * 24000)));
            data[i * 2] = (byte) (s & 0xFF);
            data[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return data;
    }

    // Rage explosion / power-up sound
    private static byte[] createRageSound(int sampleRate) {
        int length = (int) (sampleRate * 0.45); // 450ms
        byte[] data = new byte[length * 2];
        java.util.Random rnd = new java.util.Random(123);

        for (int i = 0; i < length; i++) {
            double t = (double) i / sampleRate;
            double env = Math.min(1.0, t * 15.0) * Math.exp(-t * 4.5);
            double f = 110.0 + t * 450.0; // rising pitch
            double wave = Math.sin(2.0 * Math.PI * f * t) + 0.3 * Math.sin(4.0 * Math.PI * f * t);
            double noise = (rnd.nextDouble() * 2.0 - 1.0) * 0.35;
            double sample = (wave * 0.65 + noise * 0.35) * env;
            short s = (short) Math.max(-32767, Math.min(32767, (int) (sample * 27000)));
            data[i * 2] = (byte) (s & 0xFF);
            data[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return data;
    }

    // Dodge roll sound: whoosh noise sweep
    private static byte[] createRollSound(int sampleRate) {
        int length = (int) (sampleRate * 0.20); // 200ms
        byte[] data = new byte[length * 2];
        java.util.Random rnd = new java.util.Random(77);

        for (int i = 0; i < length; i++) {
            double t = (double) i / sampleRate;
            double env = Math.sin(Math.PI * (t / 0.20));
            double noise = rnd.nextDouble() * 2.0 - 1.0;
            double sample = noise * env * 0.6;
            short s = (short) Math.max(-32767, Math.min(32767, (int) (sample * 20000)));
            data[i * 2] = (byte) (s & 0xFF);
            data[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return data;
    }
}
