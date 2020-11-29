import java.util.*;

import fj.Hash;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Cons;


public class AndersonAnalyser extends ForwardFlowAnalysis<Unit, Environ> {

	public Map<Integer, List<Integer>> ptsto;

	public HashMap<Integer, Value> queryobj = new HashMap<>();
	public HashMap<Integer, Unit> querydst = new HashMap<>();
	public ArrayList<Integer> queryid = new ArrayList<>();


	public ExceptionalUnitGraph eg;
	public SootMethod sootMethod;
	public String methodname;

	public PATransformer bg;
	public Environ backgroundFlow;
	public Value caller;
	public Value newthis = null;

//	public Qvar savedQ = null;
	public HashSet<Integer> savedRef = new HashSet<>();

	public List<Value> pArgs = null;
	public int idxArgs = 0;

	public HashSet<Value> localAdded = new HashSet<>();

	AndersonAnalyser(SootMethod _sootMethod, ExceptionalUnitGraph _eg,
					 PATransformer paTransformer, Environ bf, Value _caller, List<Value> _pArgs) {
		super(_eg);

		eg = _eg;
		sootMethod = _sootMethod;
		methodname = sootMethod.getName();

		bg = paTransformer;
		ptsto = paTransformer.ptsto;

		backgroundFlow = bf;
		caller = _caller;
		pArgs = _pArgs;

		doAnalysis();

		output();
	}


	@Override
	protected void flowThrough(Environ in, Unit unit, Environ out) {
		out.copy(in);

		//System.out.println(unit.toString());
		if (unit instanceof IdentityStmt) {
//			System.out.println(unit.toString());
//			System.out.println(((IdentityStmt) unit).getLeftOp().toString());

			IdentityStmt identityStmt = (IdentityStmt) unit;
			Value left = identityStmt.getLeftOp(), right = identityStmt.getRightOp();

			if(right.toString().contains("@this") && newthis == null) saveAddThis(left, out);
			else if(right.toString().contains("@parameter") && pArgs != null && pArgs.size() > idxArgs)
			{
				HashSet<Qvar> rs = new HashSet<>();
				rs.add(out.getCreate(pArgs.get(idxArgs), this));
				Qvar ls = out.getCreate(left, this);
				ls.assignRep(rs, out);
				idxArgs = idxArgs + 1;
			}

		} else if (unit instanceof AssignStmt) {
			AssignStmt identityStmt = (AssignStmt) unit;
			Value left = identityStmt.getLeftOp(), right = identityStmt.getRightOp();

			HashSet<Qvar> rs = new HashSet<>();

			if (right instanceof AnyNewExpr) {
				rs.add(new Qvar(right, out.getAllocid(), out));
			} else if (right instanceof Constant) ;
			else if (right instanceof Local)
				rs.add(out.getCreate((Local) right, this));
			else if (right instanceof FieldRef) {
				SootFieldRef sootFieldRef = ((FieldRef) right).getFieldRef();
				if (right instanceof InstanceFieldRef) {
					Local localF = (Local) ((InstanceFieldRef) right).getBase();
					out.getFields(rs, localF, right, sootFieldRef);
				}
			} else return;

			if (rs.size() != 0) {
				if (left instanceof Local) {
					Qvar lvar = out.getCreate((Local) left, this);
					lvar.assignRep(rs, out);
				} else if (left instanceof FieldRef) {
					SootFieldRef sootFieldRef = ((FieldRef) left).getFieldRef();
					if (left instanceof InstanceFieldRef) {
						Local localF = (Local) ((InstanceFieldRef) left).getBase();
						Qvar lvar = out.getCreate(localF, this);

						HashSet<Value> ls = new HashSet<>();
						for (Integer i : lvar.ptr) {
							ls.removeAll(out.ref.get(i));
							ls.addAll(out.ref.get(i));
						}
						for (Value value : ls) {
							Qvar posvar = out.getCreate(value, this);
							posvar.fieldAss(sootFieldRef, value, rs, out);
						}
					}
				}
			}

			for (Qvar qvar : rs) {
				if (qvar.value.toString().contains("new "))
					out.removeValue(qvar.value);
			}

		} else if (unit instanceof InvokeStmt) {
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
			List<Value> invokeArgs = invokeExpr.getArgs();

			if (invokeExpr == null) return;

			SootMethod invokemethod = invokeExpr.getMethod();


			if (invokeExpr instanceof InstanceInvokeExpr) {
				if (invokemethod.getSignature().contains("java.lang.Object: void <init>")) return;
//				System.out.print("Calling    :");
//				System.out.println(invokemethod.getSignature());

				Value ib = ((InstanceInvokeExpr) invokeExpr).getBase();
				AndersonAnalyser next = new AndersonAnalyser(invokemethod,
						new ExceptionalUnitGraph(invokemethod.getActiveBody()), bg, out, ib, invokeArgs);

			} else if (invokemethod.getSignature().contains("benchmark.internal")) {
				if (invokemethod.getSignature().contains("void alloc")) {
					out.allocvalid = true;
					out.allocid = ((IntConstant) invokeArgs.get(0)).value;
				} else if (invokemethod.getSignature().contains("void test")) {
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
	protected Environ entryInitialFlow() {
		Environ res = new Environ();
		res.copy(backgroundFlow);
		return res;
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

	public void saveAddThis(Value newv, Environ e)
	{
		newthis = newv;
//		savedQ = e.l2q.get(oldv);
//
//		if(savedQ != null)
//		{
//			e.l2q.remove(oldv);
//			for (Integer i : e.ref.keySet())
//			{
//				if (e.ref.get(i).contains(oldv))
//				{
//					e.ref.get(i).remove(oldv);
//					savedRef.add(i);
//				}
//			}
//		}
		HashSet<Qvar> rs = new HashSet<>();
		rs.add(e.getCreate(caller, this));
		Qvar ls = e.getCreate(newv, this);
		ls.assignRep(rs, e);
	}

	public void garCollection(Environ e)
	{
		Vector<Value> q = new Vector<>();
		q.addAll(localAdded);

		while(!q.isEmpty())
		{
			Value head = q.firstElement();
			q.remove(head);
			Qvar val = e.l2q.get(head);

			if(val == null) continue;
			for(SootFieldRef s: val.fields.keySet())
			{
				if(!localAdded.contains(val.fields.get(s))) q.add(val.fields.get(s));
			}
		}

		ArrayList<Integer> garbage = new ArrayList<>();
		for(Integer i : e.ref.keySet())
		{
			e.ref.get(i).removeAll(localAdded);
			if(e.ref.get(i).size() == 0) garbage.add(i);
		}
		for(Integer i : garbage) e.ref.remove(i);

		for(Value del : localAdded)
			e.l2q.remove(del);
//		if(savedQ != null)
//		{
//			e.l2q.put(caller, savedQ);
//			for(Integer i : savedRef)
//			{
//				e.ref.get(i).add(caller);
//			}
//		}
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
			else if(obj instanceof FieldRef)//not to used
			{
				ArrayList<Integer> pt = new ArrayList<>();
				SootFieldRef sootFieldRef = ((FieldRef) obj).getFieldRef();
				if(obj instanceof InstanceFieldRef)
				{
					Local localF = (Local) ((InstanceFieldRef) obj).getBase();
					Qvar lvar = e.getCreate(localF, this);
					for(Integer j : lvar.ptr)
					{
						HashSet<Value> ls = e.ref.get(j);
						for(Value value : ls)
						{
							Qvar posvar = e.getCreate(value, this);
							posvar.listConverge(sootFieldRef, pt, e);
						}
					}
				}

				Generator.output(i, pt);
			}
		}


		List<Unit> tails = graph.getTails();
		Environ ans = new Environ();
		Environ last = new Environ();
		for(Unit u: tails)
		{
			ans = new Environ();
			ans.merge(last, getFlowAfter(u));
			last.copy(ans);
		}

		backgroundFlow.copy(ans);
		garCollection(backgroundFlow);
	}

}
