import soot.options.Options;
import soot.PackManager;
import soot.Transform;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // java -jar analyzer.jar [src] [SomePackage.Main]
        if (args.length < 2) {
            System.out.println("Usage: java Main <path-to-class> <classname>");
            return;
        }
        String classpath = args[0]
                + File.pathSeparator + args[0] + File.separator + "rt.jar"
                + File.pathSeparator + args[0] + File.separator + "jce.jar";
        System.out.println(classpath);

        Options.v().set_verbose(true);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new PATransformer(args[1])));
        soot.Main.main(new String[] {
                "-w",
                "-p", "cg.spark", "enabled:true",
                "-p", "wjtp.myTransform", "enabled:true",
                "-soot-class-path", classpath,
                args[1]
        });
    }
}
