import com.entegre.launcher.Launcher;
import javax.swing.*;
import java.awt.*;
public final class MigrationProcessDriver {
    public static void main(String[] args) {
        Thread automation=new Thread(()->{
            while(true) {
                try {
                    SwingUtilities.invokeAndWait(()->{
                        for(Window window:Window.getWindows()) if(window.isShowing()) closeOption(window);
                    });
                    Thread.sleep(50);
                } catch(Exception failure) { System.exit(90); }
            }
        });
        automation.setDaemon(true); automation.start(); Launcher.main(args);
    }
    private static void closeOption(Container parent) {
        for(Component c:parent.getComponents()) {
            if(c instanceof JButton b && b.getText().equals("Tamam")) b.doClick();
            else if(c instanceof Container nested) closeOption(nested);
        }
    }
}
