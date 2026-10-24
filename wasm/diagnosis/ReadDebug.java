import java.nio.file.*;
import java.util.Arrays;
import org.teavm.backend.wasm.debug.ExternalDebugFile;
import org.teavm.backend.wasm.debug.parser.LinesDeobfuscationParser;

public class ReadDebug {
    public static void main(String[] args) throws Exception {
        var parser = new LinesDeobfuscationParser();
        ExternalDebugFile.read(Files.readAllBytes(Path.of(args[0])), name ->
                parser.canHandleSection(name) ? (bytes, start, end) ->
                        parser.applySection(name, Arrays.copyOfRange(bytes, start, end)) : null);
        for (int i = 1; i < args.length; ++i) {
            int address = Integer.decode(args[i]);
            var frames = parser.getLineInfo().deobfuscateSingle(address);
            System.out.println("address " + address);
            if (frames != null) for (var frame : frames) {
                System.out.println(frame.method.fullName() + " "
                        + (frame.file == null ? "" : frame.file.fullName()) + ":" + frame.line);
            }
        }
    }
}
