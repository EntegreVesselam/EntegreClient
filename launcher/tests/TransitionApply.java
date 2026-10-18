import com.entegre.transition.BootstrapTransition;
import java.nio.file.Path;

/** Synthetic fixture entry point; the shipped Fabric adapter calls the same core. */
public final class TransitionApply {
    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Synthetic game directory required");
        System.out.println("TRANSITION " + BootstrapTransition.apply(Path.of(args[0])));
    }
}
