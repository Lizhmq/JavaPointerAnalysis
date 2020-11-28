import java.util.*;

import fj.Hash;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Cons;


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

	public HashMap<Local, Qvar> l2q = new HashMap<>();

	AndersonAnalyser(SootMethod _sootMethod, ExceptionalUnitGraph _eg, PATransformer paTransformer) {
		super(_eg);

		eg = _eg;
		sootMethod = _sootMethod;
		methodname = sootMethod.getName();

//		queryid = paTransformer.queryid;
//		queries = paTransformer.queries;
		ptsto = paTransformer.ptsto;

		doAnalysis();

		output();
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
			AssignStmt identityStmt = (AssignStmt) unit;
			Value left = identityStmt.getLeftOp(), right = identityStmt.getRightOp();

			HashSet<Qvar> rs = new HashSet<>();

			if(right instanceof AnyNewExpr)
			{
				rs.add(new Qvar(right, out.getAllocid(), out));
			}
			else if(right instanceof Constant);
			else if(right instanceof Local)
				rs.add(out.getCreate((Local) right));
			else if(right instanceof FieldRef)
			{
				SootFieldRef sootFieldRef = ((FieldRef) right).getFieldRef();
				if(right instanceof InstanceFieldRef)
				{
					Local localF = (Local) ((InstanceFieldRef) right).getBase();
					out.getFields(rs, localF, right, sootFieldRef);
				}
			}
			else return;

			if(rs.size() != 0)
			{
				if(left instanceof Local)
				{
					Qvar lvar = out.getCreate((Local) left);
					lvar.assignRep(rs, out);
				}
				else if(left instanceof FieldRef)
				{
					SootFieldRef sootFieldRef = ((FieldRef) left).getFieldRef();
					if(left instanceof InstanceFieldRef)
					{
						Local localF = (Local) ((InstanceFieldRef) left).getBase();
						Qvar lvar = out.getCreate(localF);

						HashSet<Value> ls = new HashSet<>();
						for(Integer i : lvar.ptr)
						{
							ls.removeAll(out.ref.get(i));
							ls.addAll(out.ref.get(i));
						}
						for(Value value : ls)
						{
							Qvar posvar = out.getCreate(value);
							posvar.fieldAss(sootFieldRef, value, rs, out);
						}
					}
				}
			}

			for(Qvar qvar: rs)
			{
				if(qvar.value.toString().contains("new benchmark.objects"))
					out.removeValue(qvar.value);
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
		out.merge(in1, in2);
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
			Environ e = getFlowAfter(unit);
//			ArrayList<Integer> ans = new ArrayList<>(environ.lattice.get(obj));
//			ptsto.put(i, ans);

			if(obj instanceof Local)
			{
				Qvar qvar = e.l2q.get(obj);
				ArrayList<Integer> pt = qvar.ptr;
				Generator.output(i, pt);
			}
			else if(obj instanceof FieldRef)
			{
				ArrayList<Integer> pt = new ArrayList<>();
				SootFieldRef sootFieldRef = ((FieldRef) obj).getFieldRef();
				if(obj instanceof InstanceFieldRef)
				{
					Local localF = (Local) ((InstanceFieldRef) obj).getBase();
					Qvar lvar = e.getCreate(localF);
					for(Integer j : lvar.ptr)
					{
						HashSet<Value> ls = e.ref.get(j);
						for(Value value : ls)
						{
							Qvar posvar = e.getCreate(value);
							posvar.listConverge(sootFieldRef, pt, e);
						}
					}
				}

				Generator.output(i, pt);
			}
		}
	}

}
