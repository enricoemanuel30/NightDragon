import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateSpriteViewer {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("res/sprites/player/player_sheet.png"));
        File outDir = new File("debug_crops");
        outDir.mkdirs();

        // Let's inspect row 0 Seg 7 (657..836)
        // Check density in columns 657..836
        System.out.println("Row 0: 657 to 836 density:");
        for (int x = 657; x <= 836; x++) {
            int c = 0;
            for (int y = 10; y <= 195; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 20) c++;
            }
            if (c < 10) {
                System.out.printf("  low density col: x=%d, count=%d\n", x, c);
            }
        }

        // Row 2: 6 to 747 density
        System.out.println("\nRow 2: 6 to 747 low density points:");
        for (int x = 6; x <= 747; x++) {
            int c = 0;
            for (int y = 356; y <= 520; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 20) c++;
            }
            if (c < 15) {
                System.out.printf("x=%d(c=%d) ", x, c);
            }
        }
        System.out.println();

        // Row 3: 16 to 408 and 626 to 917 low density points
        System.out.println("\nRow 3: 16 to 408 low density points:");
        for (int x = 16; x <= 408; x++) {
            int c = 0;
            for (int y = 521; y <= 680; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 20) c++;
            }
            if (c < 15) {
                System.out.printf("x=%d(c=%d) ", x, c);
            }
        }
        System.out.println();

        System.out.println("\nRow 3: 626 to 917 low density points:");
        for (int x = 626; x <= 917; x++) {
            int c = 0;
            for (int y = 521; y <= 680; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 20) c++;
            }
            if (c < 15) {
                System.out.printf("x=%d(c=%d) ", x, c);
            }
        }
        System.out.println();
    }
}
