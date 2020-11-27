import java.util.*;

import com.sun.tools.doclint.Env;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;


public class AndersonAnalyser extends ForwardFlowAnalysis<Unit, Environ> {

//	public ArrayList<Integer> queryid;
//	public HashMap<Integer, Value> queries;
	public Map<Integer, List<Integer>> ptsto;

	public HashMap<Integer, Value> queryobj = new HashMap<>();
	public HashMap<Integer, Unit> querydst = new HashMap<>();
	public ArrayList<Integer> queryid = new ArrayList<>();


	public ExceptionalUnitGraph eg;
	public SootMethod sootMethod;
	public String methodname;

	public HashMap<Value, Unit> v2u = new HashMap<>();

	AndersonAnalyser(SootMethod _sootMethod, ExceptionalUnitGraph _eg, PATransformer paTransformer) {
		super(_eg);

		eg = _eg;
		sootMethod = _sootMethod;
		methodname = sootMethod.getName();

//		queryid = paTransformer.queryid;
//		queries = paTransformer.queries;
		ptsto = paTransformer.ptsto;

		doAnalysis();

		//output();
	}


	@Override
	protected void flowThrough(Environ in, Unit unit, Environ out) {
		out.copy(in);

		if(unit instanceof IdentityStmt)
		{
//			System.out.println(unit.toString());
//			System.out.println("IdentityStmt");
			IdentityStmt identityStmt = (IdentityStmt) unit;
			Value left = identityStmt.getLeftOp(), right = identityStmt.getRightOp();
		}
		else if(unit instanceof AssignStmt)
		{
//			System.out.println(unit.toString());
//			System.out.println("AssignStmt");
			AssignStmt identityStmt = (AssignStmt) unit;
			Value left = identityStmt.getLeftOp(), right = identityStmt.getRightOp();
			v2u.put(left, unit);

			if(right instanceof AnyNewExpr)
			{
				ArrayList<Integer> newset = new ArrayList<>();
				newset.add(out.getAllocid());
				if(out.lattice.containsKey(left)) out.lattice.replace(right, newset);
				else out.lattice.put(left, newset);
				System.out.println(unit.toString());
			}
			else if(right instanceof Local)
			{

			}
		}
		else if(unit instanceof InvokeStmt)
		{
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
			List<Value> invokeArgs = invokeExpr.getArgs();

			if(invokeExpr == null) return;

			SootMethod invokemethod = invokeExpr.getMethod();


			if(invokeExpr instanceof InstanceInvokeExpr)
			{

			}
			else if(invokemethod.getSignature().contains("benchmark.internal"))
			{
				if(invokemethod.getSignature().contains("void alloc"))
				{
					out.allocvalid = true;
					out.allocid = ((IntConstant) invokeArgs.get(0)).value;
				}
				else if(invokemethod.getSignature().contains("void test"))
				{
					int id = ((IntConstant) invokeArgs.get(0)).value;
					Value obj = invokeArgs.get(1);
					queryid.add(id);
					queryobj.put(id, obj);
					querydst.put(id, unit);
//					System.out.printf("Query %s : %d\n", obj.toString(), id);
				}
			}
		}

	}

	@Override
	protected Environ newInitialFlow() {
		return new Environ();
	}

	@Override
	protected void merge(Environ in1, Environ in2, Environ out) {
		out.copy(in1);

		for(Value value: in2.lattice.keySet())
		{
			if(out.lattice.containsKey(value))
			{
				for(Integer i : in2.lattice.get(value))
				{
					out.lattice.get(value).removeAll(in2.lattice.get(value));
					out.lattice.get(value).addAll(in2.lattice.get(value));
				}
			}
			else out.lattice.put(value, new ArrayList<>(in2.lattice.get(value)));
		}
	}

	@Override
	protected void copy(Environ src, Environ dst) {
		dst.copy(src);
	}

	void output()
	{
		Collections.sort(queryid);
		for(int i : queryid)
		{
			Unit unit = querydst.get(i);
			Value obj = queryobj.get(i);
			Environ environ = getFlowAfter(unit);
			ArrayList<Integer> ans = new ArrayList<>(environ.lattice.get(obj));
			ptsto.put(i, ans);
		}
	}

}
