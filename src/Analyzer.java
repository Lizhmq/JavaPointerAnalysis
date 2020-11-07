import soot.*;
import soot.options.*;
import soot.Scene;
import soot.PackManager;
import soot.Transform;
import java.util.*;

public class Analyzer {
    public static void main(String[] args) {
        Options.v().set_verbose(true);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new PATransformer()));

        PhaseOptions.v().setPhaseOption("jap.npc", "on");
        soot.Main.main(new String[] {
                 "-w", "-f", "J",
                // "-p", "cg.spark", "enabled:true",
                 "-p", "wjtp.myTransform", "enabled:true",
                 "-cp", "../sootOutput", "-pp",
                // "-soot-class-path",
                // "/home/lzzz/proj/soot/sootclasses-trunk-jar-with-dependencies.jar",
                "HelloWorld" 
            });
    }
}
