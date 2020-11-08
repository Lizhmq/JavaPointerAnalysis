import java.util.*;
import soot.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;


public class AndersonAnalyser extends ForwardFlowAnalysis<Unit, Object> {

	public ArrayList<Object []> querys = new ArrayList<Object []>();
	public Map<Local, Set<Integer>> ptsto = new HashMap<Local, Set<Integer>>();

	AndersonAnalyser(SootMethod mainMethod) {
		super(getGraph(mainMethod));
		doAnalysis();
	}

	static DirectedGraph<Unit> getGraph(SootMethod m) {
		return new ExceptionalUnitGraph(m.getActiveBody());
	}

	@Override
	protected void flowThrough(Object _in, Unit _node, Object _out) {

	}

	@Override
	protected void copy(Object _in, Object _out) {

	}


	@Override
	protected void merge(Object _in1, Object _in2, Object _out) {

	}

	@Override
	protected Object newInitialFlow() {
		return null;
	}
	
}
