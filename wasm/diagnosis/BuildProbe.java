import java.io.File;
import org.teavm.tooling.*;
import org.teavm.vm.TeaVMOptimizationLevel;

public class BuildProbe {
    public static void main(String[] args) throws Exception {
        TeaVMTool tool = new TeaVMTool();
        tool.setTargetType(TeaVMTargetType.WEBASSEMBLY_GC);
        tool.setMainClass(args[0]);
        tool.setTargetDirectory(new File(args[1]));
        tool.setTargetFileName("probe.wasm");
        tool.setOptimizationLevel(TeaVMOptimizationLevel.valueOf(args[2]));
        tool.setObfuscated(false);
        tool.setDebugInformationGenerated(true);
        tool.setClassLoader(BuildProbe.class.getClassLoader());
        tool.setLog(new ConsoleTeaVMToolLog(false));
        tool.generate();
        for (var p : tool.getProblemProvider().getProblems()) {
            System.out.println(p.getSeverity() + " " + p.getText() + " " + java.util.Arrays.toString(p.getParams()));
        }
        if (!tool.getProblemProvider().getSevereProblems().isEmpty()) System.exit(1);
    }
}
