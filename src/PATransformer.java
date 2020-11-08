import java.util.*;
import soot.*;


public class PATransformer extends SceneTransformer {

	protected String className;
	protected String methodName;
	PATransformer(String className) {
		super();
		this.className = className;
		this.methodName = "main";
	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		SootClass mainClass = Scene.v().getSootClass(className);
		SootMethod method = mainClass.getMethodByName(methodName);
		AndersonAnalyser analyser = new AndersonAnalyser(method);
		Generator gen = new Generator(analyser.querys, analyser.ptsto);
		gen.WriteTo("result.txt");
	}
}
