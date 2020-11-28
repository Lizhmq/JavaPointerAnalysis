
import soot.*;


import java.util.*;

import java.util.HashMap;

public class Environ {

    public int allocid;
    public boolean allocvalid;

    public HashMap<Value, Qvar> l2q = new HashMap<>();
    public HashMap<Integer, HashSet<Value> > ref = new HashMap<>();
//    public HashMap<Integer, HashMap<SootFieldRef, Local> > fields = new HashMap<>();

    //    public HashMap<Value, Rval> v2r = new HashMap<>();

    Environ()
    {
        allocid = 0;
        allocvalid = false;
    }

    Environ(Environ e)
    {
        allocid = e.allocid;
        allocvalid = e.allocvalid;

        for(Value value : e.l2q.keySet())
            l2q.put(value, new Qvar(e.l2q.get(value), this));

        for(Integer i : e.ref.keySet())
        {
            HashSet<Value> n1 = new HashSet<>(e.ref.get(i));
            ref.put(i, n1);
        }

//        HashMap<Qvar, Local> q2l = new HashMap<>();
//        HashMap<Rval, Value> r2v = new HashMap<>();
//        for(Local local : e.l2q.keySet())
//        {
//            Qvar qvar = l2q.get(local).copy();
//            l2q.put(local, qvar);
//            q2l.put(qvar, local);
//        }
//
//        for(Value value: e.v2r.keySet())
//        {
//            Rval rval = e.v2r.get(value).copy(q2l, l2q);
//            v2r.put(value, rval);
//            r2v.put(rval, value);
//        }
//
//        for(Local local: l2q.keySet())
//        {
//            Qvar qvar = l2q.get(local);
//            qvar.rval = v2r.get(r2v.get(qvar.rval));
//        }
    }

    public void copy(Environ e) {
        allocid = e.allocid;
        allocvalid = e.allocvalid;

        for(Value value : e.l2q.keySet())
            l2q.put(value, new Qvar(e.l2q.get(value), this));

        for(Integer i : e.ref.keySet())
        {
            HashSet<Value> n1 = new HashSet<>(e.ref.get(i));
            ref.put(i, n1);
        }

//        HashMap<Qvar, Local> q2l = new HashMap<>();
//        HashMap<Rval, Value> r2v = new HashMap<>();
//        for(Local local : e.l2q.keySet())
//        {
//            Qvar qvar = l2q.get(local).copy();
//            l2q.put(local, qvar);
//            q2l.put(qvar, local);
//        }
//
//        for(Value value: e.v2r.keySet())
//        {
//            Rval rval = e.v2r.get(value).copy(q2l, l2q);
//            v2r.put(value, rval);
//            r2v.put(rval, value);
//        }
//
//        for(Local local: l2q.keySet())
//        {
//            Qvar qvar = l2q.get(local);
//            qvar.rval = v2r.get(r2v.get(qvar.rval));
//        }

    }

    public int getAllocid() {
        int res = allocid;
        allocid = 0;
        allocvalid = false;
        return res;
    }

    public void getFields(HashSet<Qvar> rs, Local local, Value right, SootFieldRef sootFieldRef)
    {
        Qvar qvar = l2q.get(local);
        if(qvar == null) return;
        Value val = l2q.get(local).fields.get(sootFieldRef);
        if(val == null)
        {
            Qvar ss = new Qvar(right, -1, this);
            val = ss.value;
            l2q.get(local).fields.put(sootFieldRef, val);
        }
        rs.add(l2q.get(val));
    }


    public Qvar getCreate(Value value)
    {
        if(l2q.containsKey(value)) return l2q.get(value);
        return new Qvar(value, -1, this);
    }

    void merge(Environ in1, Environ in2)
    {
        allocvalid = in1.allocvalid || in2.allocvalid;
        allocid = in1.allocid > 0 ? in1.allocid : in2.allocid;

        for(Value value : in1.l2q.keySet())
            l2q.put(value, new Qvar(in1.l2q.get(value), this));

        for(Value value : in2.l2q.keySet())
        {
            if (l2q.containsKey(value)) l2q.get(value).assignMer(in2.l2q.get(value), this);
            else l2q.put(value, new Qvar(in2.l2q.get(value), this));
        }
//
//        allocvalid = e1.allocvalid || allocvalid;
//        allocid = e1.allocid > 0 ? e1.allocid : allocid;
//
//        for(Local local : e1.l2q.keySet())
//        {
//            if(l2q.containsKey(local))
//            {
//                Qvar le = l2q.get(local), ri = e1.l2q.get(local);
//
//            }
//        }



//        for(Value value : e1.v2r.keySet())
//        {
//
//            if(e2.v2r.containsKey(value))
//            {
//                Rval rval = e1.v2r.get(value).copy();
//
//            }
//
//        }
//
//        for(Local local : e1.l2q.keySet())
//        {
//            Qvar q = e1.l2q.get(local).copy();
//            q.ptr.removeAll(e2.l2q.get(local).ptr);
//            q.ptr.addAll(e2.l2q.get(local).ptr);
//
//        }
    }

    void removeValue(Value tmp)
    {
        l2q.remove(tmp);
        for(Integer i: ref.keySet())
            if(ref.get(i).contains(tmp)) ref.get(i).remove(tmp);
    }


    @Override
    public boolean equals(Object obj) {
        Environ e = (Environ) obj;

        if(allocvalid != e.allocvalid) return false;
        if(l2q.size() != e.l2q.size() || ref.size() != e.l2q.size()) return false;

        for(Value value: l2q.keySet())
        {
            if(!e.l2q.containsKey(value)) return false;
            if(l2q.get(value).ptr.size() != e.l2q.get(value).ptr.size() ||
                    l2q.get(value).fields.size() != e.l2q.get(value).fields.size()) return false;
            for(Integer i : l2q.get(value).ptr)
                if(!e.l2q.get(value).ptr.contains(i)) return false;

            for(SootFieldRef s : l2q.get(value).fields.keySet())
                if(!e.l2q.get(value).fields.containsKey(s) ||
                        l2q.get(value).fields.get(s) != e.l2q.get(value).fields.get(s)) return false;
        }

        for(Integer i : ref.keySet())
        {
            if(!e.ref.containsKey(i)) return false;
            if(ref.get(i).size() != e.ref.get(i).size()) return false;

            for(Value value: ref.get(i))
                if(!e.ref.get(i).contains(value)) return false;

        }

        return true;
    }

}
