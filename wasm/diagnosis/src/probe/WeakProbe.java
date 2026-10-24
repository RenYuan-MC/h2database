package probe;
import java.lang.ref.WeakReference;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import org.teavm.jso.JSExport;

public final class WeakProbe {
    private static Object strong;
    private static WeakReference<Object> weak;
    private static Reference<Object> phantom;
    @JSExport public static int run(int input) {
        strong = new int[] { input };
        phantom = new PhantomReference<>(strong, new ReferenceQueue<>());
        weak = new WeakReference<>(strong);
        Object seen = phantom.get();
        Object got = weak.get();
        weak.clear();
        return got == strong && seen == null ? ((int[]) got)[0] : -1;
    }
}
