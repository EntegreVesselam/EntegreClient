package link.infra.packwiz.installer.ui.gui;

import com.entegre.launcher.ProgressText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicProgressBarUI;
import link.infra.packwiz.installer.ui.data.InstallProgress;

/** Drop-in replacement for the pinned Packwiz installer's main window. */
public final class InstallWindow extends JFrame {
    private static final Color BACKGROUND = new Color(24, 25, 23);
    private static final Color SURFACE = new Color(38, 40, 36);
    private static final Color FOREGROUND = new Color(244, 240, 229);
    private static final Color MUTED = new Color(174, 177, 163);
    private static final Color ACCENT = new Color(227, 179, 92);
    private static final Color OUTLINE = new Color(73, 76, 66);

    private final GUIHandler handler;
    private final boolean settingsMode = Boolean.getBoolean("entegre.settings");
    private final boolean preview = Boolean.getBoolean("entegre.preview");
    private JLabel status;
    private JLabel detail;
    private JLabel count;
    private JProgressBar progressBar;
    private JButton options;
    private JButton cancel;
    private JButton ok;
    private boolean settingsRequested;

    public InstallWindow(GUIHandler handler) {
        this.handler = handler;
        if (SwingUtilities.isEventDispatchThread()) {
            initialize();
        } else {
            try {
                SwingUtilities.invokeAndWait(this::initialize);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Güncelleme penceresi açılamadı", interrupted);
            } catch (InvocationTargetException failed) {
                throw new IllegalStateException("Güncelleme penceresi açılamadı", failed.getCause());
            }
        }
    }

    private void initialize() {
        setTitle("Entegre Vesselam");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // JFrame's EXIT_ON_CLOSE reports success, which would let Prism launch after an interrupted update.
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                System.exit(preview ? 0 : 1);
            }
        });
        setMinimumSize(new Dimension(580, 380));
        setSize(680, 400);
        setLocationRelativeTo(null);

        JPanel root = panel(new BorderLayout(0, 28));
        root.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        setContentPane(root);

        JPanel header = panel(new BorderLayout(18, 0));
        URL logo = InstallWindow.class.getResource("/entegre/logo.png");
        if (logo != null) {
            ImageIcon original = new ImageIcon(logo);
            setIconImage(original.getImage());
            JLabel mark = new JLabel(new ImageIcon(original.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            mark.getAccessibleContext().setAccessibleName("Entegre Vesselam logosu");
            header.add(mark, BorderLayout.WEST);
        }
        JPanel brand = panel(new java.awt.GridLayout(2, 1));
        brand.add(label("ENTEGRE", 27, Font.BOLD, FOREGROUND));
        brand.add(label("VESSELAM", 12, Font.PLAIN, MUTED));
        header.add(brand, BorderLayout.CENTER);
        JLabel badge = label(preview ? "ÖNİZLEME" : settingsMode ? "MOD AYARLARI" : "GÜNCELLEYİCİ", 10, Font.BOLD, ACCENT);
        header.add(badge, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel progress = panel(null);
        progress.setLayout(new BoxLayout(progress, BoxLayout.Y_AXIS));
        status = label("Güncellemeler hazırlanıyor", 20, Font.BOLD, FOREGROUND);
        detail = label(" ", 13, Font.PLAIN, MUTED);
        count = label("Hazırlanıyor", 12, Font.PLAIN, MUTED);
        progressBar = new JProgressBar();
        progressBar.setUI(new BasicProgressBarUI());
        progressBar.setBorderPainted(false);
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(SURFACE);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(100, 7));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        progressBar.setAlignmentX(0);
        progressBar.getAccessibleContext().setAccessibleName("Güncelleme ilerlemesi");
        progress.add(status);
        progress.add(Box.createVerticalStrut(8));
        progress.add(detail);
        progress.add(Box.createVerticalStrut(22));
        progress.add(progressBar);
        progress.add(Box.createVerticalStrut(10));
        progress.add(count);
        root.add(progress, BorderLayout.CENTER);

        JPanel footer = panel(new BorderLayout(0, 16));
        JLabel hint = label(preview ? "Örnek ekran · Dosya indirilmez, oyun başlatılmaz."
            : settingsMode ? "Mod tercihlerini düzenleyebilirsin."
            : "Güncelleme tamamlanınca oyun açılır.", 12, Font.PLAIN, MUTED);
        footer.add(hint, BorderLayout.NORTH);
        JPanel buttons = panel(new BorderLayout(12, 0));
        options = button("Mod ayarları", KeyEvent.VK_M, false);
        options.setToolTipText("Bu paket için isteğe bağlı modları seç");
        options.addActionListener(event -> requestOptions());
        buttons.add(options, BorderLayout.WEST);
        JPanel actions = panel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        cancel = button("İptal", KeyEvent.VK_I, false);
        cancel.addActionListener(event -> {
            if (preview) System.exit(0);
            cancel.setEnabled(false);
            handler.setCancelButtonPressed(true);
        });
        ok = button("Devam", KeyEvent.VK_D, true);
        ok.addActionListener(event -> handler.setOkButtonPressed(true));
        ok.setVisible(false);
        actions.add(cancel);
        actions.add(ok);
        buttons.add(actions, BorderLayout.EAST);
        footer.add(buttons, BorderLayout.SOUTH);
        root.add(footer, BorderLayout.SOUTH);
    }

    public final void displayProgress(InstallProgress progress) {
        onEdt(() -> {
            ProgressText.Text text = ProgressText.from(progress.getMessage());
            status.setText(text.title());
            status.setToolTipText(text.title());
            detail.setText(text.detail().isEmpty() ? " " : text.detail());
            detail.setToolTipText(text.detail().isEmpty() ? null : text.detail());
            if (progress.getHasProgress() && progress.getProgressTotal() > 0) {
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(progress.getProgressTotal());
                progressBar.setValue(progress.getProgress());
                String message = progress.getMessage();
                count.setText(message.equals("Reconfigure optional mods?") ? "Seçim bekleniyor"
                    : message.equals("Modpack is already up to date!") || message.equals("Modpack files are already up to date!")
                        ? "Hazır" : progress.getProgress() + " / " + progress.getProgressTotal() + " dosya");
            } else {
                progressBar.setIndeterminate(true);
                progressBar.setValue(0);
                count.setText("İşlem sürüyor");
            }
            progressBar.getAccessibleContext().setAccessibleDescription(status.getText() + ", " + count.getText());
        });
    }

    public final void disableOptionsButton(boolean hasOptions) {
        onEdt(() -> {
            options.setText(hasOptions ? "Mod ayarları" : "Ek mod yok");
            options.setEnabled(false);
            options.setToolTipText(hasOptions ? "Mod seçimleri işleniyor" : "Bu pakette isteğe bağlı mod bulunmuyor");
        });
    }

    public final void showOk(boolean hideCancel) {
        onEdt(() -> {
            cancel.setVisible(!hideCancel);
            ok.setVisible(true);
            getRootPane().setDefaultButton(ok);
            if (settingsMode && !settingsRequested && options.isEnabled()) {
                settingsRequested = true;
                requestOptions();
            }
            getContentPane().revalidate();
            getContentPane().repaint();
        });
    }

    public final void hideOk() {
        onEdt(() -> {
            ok.setVisible(false);
            cancel.setVisible(true);
            getRootPane().setDefaultButton(null);
            getContentPane().revalidate();
            getContentPane().repaint();
        });
    }

    public final void timeoutOk(long remaining) {
        // The launcher uses timeout=0; keep this ABI without adding a decorative countdown.
        onEdt(() -> ok.setText("Devam"));
    }

    private void requestOptions() {
        options.setText("Ayarlar açılıyor…");
        options.setEnabled(false);
        handler.setOptionsButtonPressed(true);
    }

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) action.run();
        else SwingUtilities.invokeLater(action);
    }

    private static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BACKGROUND);
        return panel;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.putClientProperty("html.disable", Boolean.TRUE);
        label.setFont(new Font(Font.SANS_SERIF, style, size));
        label.setForeground(color);
        label.setAlignmentX(0);
        label.setMinimumSize(new Dimension(0, label.getPreferredSize().height));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
        return label;
    }

    private static JButton button(String text, int mnemonic, boolean primary) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI() {
            @Override protected void paintText(Graphics graphics, AbstractButton button, Rectangle bounds, String text) {
                if (button.isEnabled()) {
                    super.paintText(graphics, button, bounds, text);
                    return;
                }
                graphics.setColor(MUTED);
                BasicGraphicsUtils.drawStringUnderlineCharAt(graphics, text, button.getDisplayedMnemonicIndex(),
                    bounds.x, bounds.y + graphics.getFontMetrics().getAscent());
            }
        });
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBackground(primary ? ACCENT : SURFACE);
        button.setForeground(primary ? BACKGROUND : FOREGROUND);
        button.setOpaque(true);
        button.setMnemonic(mnemonic);
        button.getAccessibleContext().setAccessibleName(text);
        Border padding = BorderFactory.createEmptyBorder(11, 17, 11, 17);
        Border normal = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? ACCENT : OUTLINE), padding);
        Border focused = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FOREGROUND), padding);
        button.setBorder(normal);
        button.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) { button.setBorder(focused); }
            @Override public void focusLost(FocusEvent event) { button.setBorder(normal); }
        });
        return button;
    }
}
