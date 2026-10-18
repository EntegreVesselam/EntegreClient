import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import link.infra.packwiz.installer.ui.data.InstallProgress;
import link.infra.packwiz.installer.ui.gui.GUIHandler;
import link.infra.packwiz.installer.ui.gui.InstallWindow;

/** Run with -ea; --text runs without a display and --close checks the process exit code. */
public final class WindowTest {
    public static void main(String[] args) throws Exception {
        if (!WindowTest.class.desiredAssertionStatus()) {
            throw new IllegalStateException("Run WindowTest with -ea");
        }
        if (args.length > 0 && args[0].equals("--text")) {
            checkText();
        } else if (args.length > 0 && args[0].equals("--close")) {
            GUIHandler handler = new GUIHandler();
            InstallWindow window = window(handler);
            SwingUtilities.invokeAndWait(() -> window.dispatchEvent(
                new java.awt.event.WindowEvent(window, java.awt.event.WindowEvent.WINDOW_CLOSING)));
            throw new AssertionError("Closing the updater must terminate its process");
        } else {
            checkWindow();
        }
        System.out.println("WindowTest passed");
    }

    private static void checkText() throws Exception {
        Class<?> type;
        try {
            type = Class.forName("com.entegre.launcher.ProgressText");
        } catch (ClassNotFoundException missing) {
            throw new AssertionError("The upstream updater has no Turkish progress translation", missing);
        }
        Method from = type.getMethod("from", String.class);
        String[][] cases = {
            {"Loading manifest file...", "Kurulum bilgileri okunuyor", ""},
            {"Loading pack file...", "Güncellemeler kontrol ediliyor", ""},
            {"Loading MultiMC pack file...", "Oyun sürümü kontrol ediliyor", ""},
            {"Checking local files...", "Yerel dosyalar kontrol ediliyor", ""},
            {"Comparing new files...", "Dosya değişiklikleri karşılaştırılıyor", ""},
            {"Validating existing files...", "Dosyalar doğrulanıyor", ""},
            {"Resolving CurseForge metadata...", "Mod bilgileri alınıyor", ""},
            {"Modpack is already up to date!", "Her şey güncel", ""},
            {"Modpack files are already up to date!", "Her şey güncel", ""},
            {"Reconfigure optional mods?", "Mod ayarları hazır", ""},
            {"Downloaded mods/çok-uzun.jar", "Dosya indirildi", "mods/çok-uzun.jar"},
            {"Downloading mods/example.jar", "Dosya indiriliyor", "mods/example.jar"},
            {"mods/a.jar already exists (cached)", "Dosya güncel", "mods/a.jar"},
            {"mods/a.jar already exists (validated)", "Dosya doğrulandı", "mods/a.jar"},
            {"Skipped mods/a.jar (disabled)", "Devre dışı dosya atlandı", "mods/a.jar"},
            {"Skipped mods/a.jar (wrong side)", "Bu kurulum için gerekmeyen dosya atlandı", "mods/a.jar"},
            {"Deleted mods/a.jar (disabled)", "Devre dışı dosya kaldırıldı", "mods/a.jar"},
            {"Deleted mods/a.jar (wrong side)", "Bu kurulum için gerekmeyen dosya kaldırıldı", "mods/a.jar"},
            {"mods/a.jar pending (you should never see this...)", "Dosya bekleniyor", "mods/a.jar"},
            {"Failed to download mods/a.jar: connection reset", "Dosya indirilemedi", "mods/a.jar: connection reset"},
            {"Unexpected engine detail", "Güncelleme sürüyor", "Unexpected engine detail"},
        };
        for (String[] expected : cases) {
            Object actual = from.invoke(null, expected[0]);
            assert actual.getClass().getMethod("title").invoke(actual).equals(expected[1]) : expected[0];
            assert actual.getClass().getMethod("detail").invoke(actual).equals(expected[2]) : expected[0];
        }
    }

    private static void checkWindow() throws Exception {
        System.clearProperty("entegre.settings");
        GUIHandler handler = new GUIHandler();
        InstallWindow window = window(handler);
        AtomicBoolean offEdt = new AtomicBoolean();
        try {
            SwingUtilities.invokeAndWait(() -> {
                assert button(window, "Mod ayarları") != null : "Main buttons must be Turkish";
                assert window.getDefaultCloseOperation() == WindowConstants.DO_NOTHING_ON_CLOSE
                    : "Window close must not report success to Prism";
                for (JButton button : components(window, JButton.class)) {
                    assert button.getMnemonic() != 0 : button.getText();
                    assert button.getAccessibleContext().getAccessibleName() != null : button.getText();
                }
                for (JComponent component : components(window, JComponent.class)) {
                    component.addPropertyChangeListener(event -> {
                        if (!SwingUtilities.isEventDispatchThread()) offEdt.set(true);
                    });
                }
            });
            window.displayProgress(new InstallProgress("Downloaded mods/example.jar", 3, 8));
            SwingUtilities.invokeAndWait(() -> {
                JProgressBar progress = components(window, JProgressBar.class).get(0);
                assert !progress.isIndeterminate();
                assert progress.getValue() == 3 && progress.getMaximum() == 8;
                assert label(window, "Dosya indirildi") != null;
                assert label(window, "mods/example.jar") != null;
                assert label(window, "3 / 8 dosya") != null;
            });
            window.displayProgress(new InstallProgress("Checking local files..."));
            SwingUtilities.invokeAndWait(() -> {
                assert components(window, JProgressBar.class).get(0).isIndeterminate();
                assert label(window, "Yerel dosyalar kontrol ediliyor") != null;
            });
            window.displayProgress(new InstallProgress("Downloaded " + "long-file-".repeat(80) + ".jar", 1, 2));
            SwingUtilities.invokeAndWait(() -> {
                JLabel detail = components(window, JLabel.class).stream()
                    .filter(l -> l.getText() != null && l.getText().startsWith("long-file-"))
                    .findFirst().orElseThrow();
                assert detail.getToolTipText().equals(detail.getText());
                assert Boolean.TRUE.equals(detail.getClientProperty("html.disable"));
            });
            window.showOk(true);
            window.timeoutOk(10);
            SwingUtilities.invokeAndWait(() -> {
                assert !button(window, "İptal").isVisible();
                JButton ok = button(window, "Devam");
                assert ok.isVisible() : "Continue must have no cosmetic countdown";
                assert window.getRootPane().getDefaultButton() == ok;
                ok.doClick(0);
                assert handler.getOkButtonPressed();
            });
            window.hideOk();
            AtomicBoolean cancellation = new AtomicBoolean();
            handler.setCancelCallback(() -> { cancellation.set(true); return kotlin.Unit.INSTANCE; });
            SwingUtilities.invokeAndWait(() -> {
                assert !button(window, "Devam").isVisible();
                JButton cancel = button(window, "İptal");
                assert cancel.isVisible();
                cancel.doClick(0);
                assert handler.getCancelButtonPressed() && cancellation.get();
                assert !cancel.isEnabled();
                assert !offEdt.get() : "Worker callbacks must marshal UI updates to the EDT";
            });
        } finally {
            handler.dispose();
        }
        checkSettings(false);
        checkSettings(true);
    }

    private static void checkSettings(boolean disabled) throws Exception {
        System.setProperty("entegre.settings", "true");
        GUIHandler handler = new GUIHandler();
        InstallWindow window = window(handler);
        try {
            if (disabled) window.disableOptionsButton(false);
            window.showOk(true);
            SwingUtilities.invokeAndWait(() -> {
                assert handler.getOptionsButtonPressed() == !disabled
                    : "Settings must select only enabled options";
                if (!disabled) handler.setOptionsButtonPressed(false);
            });
            window.showOk(true);
            SwingUtilities.invokeAndWait(() -> {
                assert !handler.getOptionsButtonPressed() : "Settings auto-open must occur exactly once";
            });
        } finally {
            handler.dispose();
            System.clearProperty("entegre.settings");
        }
    }

    private static InstallWindow window(GUIHandler handler) throws Exception {
        var field = GUIHandler.class.getDeclaredField("frmPackwizlauncher");
        field.setAccessible(true);
        return (InstallWindow) field.get(handler);
    }

    private static JButton button(Container root, String text) {
        return components(root, JButton.class).stream().filter(b -> text.equals(b.getText())).findFirst().orElse(null);
    }

    private static JLabel label(Container root, String text) {
        return components(root, JLabel.class).stream().filter(l -> text.equals(l.getText())).findFirst().orElse(null);
    }

    private static <T extends JComponent> List<T> components(Container root, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) result.add(type.cast(child));
            if (child instanceof Container container) result.addAll(components(container, type));
        }
        return result;
    }
}
