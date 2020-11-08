import soot.options.Options;
import soot.PackManager;
import soot.Transform;

public class Main {
    public static void main(String[] args) {
        // java -jar analyzer.jar [src] [SomePackage.Main]
        if (args.length != 3) {
            System.out.println("Usage: java Main <path-to-class> <classname>");
            return;
        }
        Options.v().set_verbose(true);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new PATransformer(args[2])));
        soot.Main.main(new String[] {
                "-w", "-f", "J",
                "-p", "wjtp.myTransform", "enabled:true",
                "-cp", args[1], "-pp",
                args[2]
        });
    }
}
