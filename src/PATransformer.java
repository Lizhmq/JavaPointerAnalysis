import java.util.*;

import fj.Hash;
import jdk.nashorn.internal.ir.JumpStatement;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.queue.QueueReader;
import soot.toolkits.scalar.*;


public class PATransformer extends SceneTransformer {

	protected String className;
//	protected String methodName;

//	public ArrayList<Integer> queryid = new ArrayList<>();
//	public HashMap<Integer, Value> queries = new HashMap<>();
	public Map<Integer, List<Integer>> ptsto = new HashMap<>();
	public ArrayList<String> callStack = new ArrayList<>();


	PATransformer(String _className) {
		super();
		this.className = _className;
//		this.methodName = "main";
	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		SootClass mainClass = Scene.v().getSootClass(className);
//		SootMethod method = mainClass.getMethodByName("main");
		SootMethod mainMethod = Scene.v().getMainMethod();

		AndersonAnalyser ad = new AndersonAnalyser(mainMethod, new ExceptionalUnitGraph(mainMethod.getActiveBody()),
				this, new Environ(), null, null);
		Generator.output(ptsto);
//		ad.output();

//		System.out.println("wow");

	}

}
