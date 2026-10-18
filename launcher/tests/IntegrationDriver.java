import com.entegre.launcher.Launcher;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/** Drives only the Swing windows in this synthetic installer's own JVM. */
public final class IntegrationDriver {
    private static final Set<Window> handled = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("mode and launcher arguments required");
        String mode = args[0];
        Thread automation = new Thread(() -> {
            try {
                while (true) {
                    SwingUtilities.invokeAndWait(() -> inspect(mode));
                    Thread.sleep(40);
                }
            } catch (Throwable failure) {
                failure.printStackTrace();
                System.exit(90); // Distinguish driver failure from the installer's exit code.
            }
        }, "synthetic-swing-driver");
        automation.setDaemon(true);
        automation.start();
        Launcher.main(Arrays.copyOfRange(args, 1, args.length));
    }

    private static void inspect(String mode) {
        for (Window window : Window.getWindows()) {
            if (!window.isShowing() || handled.contains(window)) continue;
            String type = window.getClass().getSimpleName();
            if (type.equals("OptionsSelectWindow")) {
                require(((javax.swing.JDialog) window).getTitle().equals("İsteğe bağlı modları seç"), "Optional dialog must default to Turkish");
                JTable table = find(window, JTable.class);
                require(table != null && table.getRowCount() == 1, "Expected one synthetic optional mod");
                require("Synthetic optional mod".equals(table.getValueAt(0, 1)), "Unexpected optional mod");
                boolean expected = mode.equals("settings-off");
                require(Boolean.valueOf(expected).equals(table.getValueAt(0, 0)), "Optional preference was not retained");
                handled.add(window);
                mark("OPTIONS initial=" + expected);
                if (mode.equals("cancel-optional")) {
                    click(window, "İptal");
                } else {
                    if (mode.equals("settings-on")) table.setValueAt(Boolean.TRUE, 0, 0);
                    if (mode.equals("settings-off")) table.setValueAt(Boolean.FALSE, 0, 0);
                    click(window, "Tamam");
                }
            } else if (type.equals("InstallWindow")) {
                if (mode.equals("cancel-main")) {
                    handled.add(window);
                    mark("CANCEL_MAIN");
                    click(window, "İptal", "Cancel");
                } else if (mode.equals("close-main")) {
                    handled.add(window);
                    mark("CLOSE_MAIN");
                    window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                }
                // Never press the main window's Continue/OK: it would hide a timeout regression.
            } else if (type.equals("ExceptionListWindow")) {
                require(((javax.swing.JDialog) window).getTitle().equals("İndirilemeyen dosyalar"), "Download failure dialog must be Turkish");
                handled.add(window);
                mark("ERROR_CANCEL");
                click(window, "Başlatmayı iptal et");
            } else if (findButton(window, "Çık") != null) {
                handled.add(window);
                mark("QUIT");
                click(window, "Çık");
            }
        }
    }

    private static void mark(String text) {
        System.out.println("INTEGRATION " + text);
        System.out.flush();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void click(Container root, String... labels) {
        JButton button = findButton(root, labels);
        require(button != null && button.isEnabled(), "Missing/enabled button: " + Arrays.toString(labels));
        button.doClick(0);
    }

    private static JButton findButton(Container root, String... labels) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && button.isShowing()
                    && Arrays.asList(labels).contains(button.getText())) return button;
            if (component instanceof Container container) {
                JButton nested = findButton(container, labels);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) return type.cast(component);
            if (component instanceof Container container) {
                T nested = find(container, type);
                if (nested != null) return nested;
            }
        }
        return null;
    }
}
