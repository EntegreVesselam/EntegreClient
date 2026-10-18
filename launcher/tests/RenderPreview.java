import java.awt.EventQueue;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import link.infra.packwiz.installer.ui.data.InstallProgress;
import link.infra.packwiz.installer.ui.gui.GUIHandler;
import link.infra.packwiz.installer.ui.gui.InstallWindow;

/** Renders this JVM's actual Swing preview; never captures the user's screen. */
public final class RenderPreview {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("PNG destination required");
        System.setProperty("entegre.preview", "true");
        GUIHandler handler = new GUIHandler();
        handler.setTitle("Entegre Vesselam — Önizleme");
        handler.show();
        handler.submitProgress(new InstallProgress("Downloading Entegre Markers", 7, 12));
        handler.disableOptionsButton(true);
        EventQueue.invokeAndWait(() -> {
            try {
                for (Window window : Window.getWindows()) {
                    if (!(window instanceof InstallWindow)) continue;
                    BufferedImage image = new BufferedImage(window.getWidth() * 2,
                            window.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
                    var graphics = image.createGraphics();
                    graphics.scale(2, 2);
                    window.printAll(graphics);
                    graphics.dispose();
                    ImageIO.write(image, "png", Path.of(args[0]).toFile());
                }
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { for (Window window : Window.getWindows()) window.dispose(); }
        });
        System.out.println("Rendered actual Swing preview.");
        System.exit(0);
    }
}
