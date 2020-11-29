
import com.sun.tools.doclint.Env;
import soot.*;
import soot.jimple.internal.AbstractInstanceFieldRef;
import soot.jimple.internal.JInstanceFieldRef;
import soot.util.Switch;

import java.util.*;


public class Qvar {
    Value value;

    ArrayList<Integer> ptr  = new ArrayList<>();
    HashMap<SootFieldRef, Value> fields = new HashMap<>();

    Qvar(Value _value, int allocid, Environ e)
    {
        value =  _value;

        Value del = null;
        if(value instanceof SootFieldRef)
        {
            for (Value tmp : e.l2q.keySet())
            {
                if (!tmp.toString().equals(value.toString())) continue;
                del = tmp;
                break;
            }
        }

        if(allocid != -1)
        {
            ptr.add(allocid);
            if(e.ref.containsKey(allocid)) e.ref.get(allocid).add(value);
            else
            {
                HashSet<Value> hs = new HashSet<>();
                hs.add(value);
                e.ref.put(allocid, hs);
            }
        }

        if(del != null)
        {
            Qvar tod = e.l2q.get(del);
            ptr.addAll(tod.ptr);
            fields.putAll(tod.fields);

            e.l2q.remove(del);
            for(Value tmp : e.l2q.keySet())
            {
                for(SootFieldRef s : e.l2q.get(tmp).fields.keySet())
                {
                    Value res = e.l2q.get(tmp).fields.get(s);
                    if(res == del) e.l2q.get(tmp).fields.replace(s, value);
                }
            }

            for(Integer i : e.ref.keySet())
            {
                if(e.ref.get(i).contains(del))
                {
                    e.ref.get(i).remove(del);
                    e.ref.get(i).add(value);
                }
            }

        }
        e.l2q.put(value, this);
    }


    Qvar (Qvar qvar, Environ e)
    {
        value = qvar.value;

        for(Integer allocid : qvar.ptr)
        {
            if(allocid != -1)
            {
                ptr.add(allocid);
                if(e.ref.containsKey(allocid)) e.ref.get(allocid).add(value);
                else
                {
                    HashSet<Value> hs = new HashSet<>();
                    hs.add(value);
                    e.ref.put(allocid, hs);
                }
            }
        }

        fields.putAll(qvar.fields);
        e.l2q.put(value, this);
    }


    public void fieldAss(SootFieldRef s, Value ls, HashSet<Qvar> rs, Environ out, boolean flag)
    {
        Value val = fields.get(s);

        if(val == null)
        {
            JInstanceFieldRef sub = new JInstanceFieldRef(ls, s);
            Qvar qvar = new Qvar(sub, -1, out);
            out.l2q.put(qvar.value, qvar);
            val = qvar.value;
        }
        fields.put(s, val);

        if(ptr.size() > 1 || flag == true) out.l2q.get(fields.get(s)).assignMer(rs, out);
        else out.l2q.get(fields.get(s)).assignRep(rs, out);
    }

    public void listConverge(SootFieldRef s, ArrayList<Integer> ans, Environ e)
    {
        Value val = fields.get(s);
        if(val == null) return;
        ans.addAll(e.l2q.get(val).ptr);
    }


    public void assignRep(Qvar qvar, Environ e)
    {
        ptr.clear();
        for(Integer i : e.ref.keySet())
            if(e.ref.get(i).contains(value)) e.ref.get(i).remove(value);

        for(Integer allocid : qvar.ptr)
        {
            if(allocid != -1)
            {
                ptr.add(allocid);
                if(e.ref.containsKey(allocid)) e.ref.get(allocid).add(value);
                else
                {
                    HashSet<Value> hs = new HashSet<>();
                    hs.add(value);
                    e.ref.put(allocid, hs);
                }
            }
        }

        fields.clear();
        fields.putAll(qvar.fields);
    }

    public void assignMer(Qvar qvar, Environ e)
    {
        ptr.removeAll(qvar.ptr);
        for(Integer allocid : qvar.ptr)
        {
            if(allocid != -1)
            {
                ptr.add(allocid);
                if(e.ref.containsKey(allocid)) e.ref.get(allocid).add(value);
                else
                {
                    HashSet<Value> hs = new HashSet<>();
                    hs.add(value);
                    e.ref.put(allocid, hs);
                }
            }
        }

        for(SootFieldRef s : qvar.fields.keySet())
        {
            if(fields.containsKey(s)) fields.replace(s, qvar.fields.get(s));
            else fields.put(s, qvar.fields.get(s));
        }
    }

    public void assignRep(HashSet<Qvar> rs, Environ e)
    {
        for(Qvar qvar : rs) assignRep(qvar, e);
    }

    public void assignMer(HashSet<Qvar> rs, Environ e)
    {
        for(Qvar qvar : rs) assignMer(qvar, e);
    }


}
