package probe;
import java.nio.channels.OverlappingFileLockException;
import org.teavm.jso.JSExport;

public final class CatchProbe {
    private static RuntimeException pending = new IllegalStateException("probe");
    @JSExport public static int run(int input) {
        try {
            if (input == 0) throw pending;
            return 0;
        } catch (OverlappingFileLockException e) {
            return 1;
        } catch (RuntimeException e) {
            return 2;
        }
    }
}
