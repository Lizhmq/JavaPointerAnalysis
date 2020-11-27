import soot.*;

import java.util.*;

import java.util.HashMap;

public class Environ {

    public int allocid;
    public boolean allocvalid;
    public HashMap<Value, ArrayList<Integer> > lattice;

    Environ()
    {
        allocid = 0;
        allocvalid = false;
        lattice = new HashMap<>();
    }
    Environ(Environ environ)
    {
        allocid = environ.allocid;
        allocvalid = environ.allocvalid;

        lattice = new HashMap<>();
        for(Value value : environ.lattice.keySet())
            lattice.put(value, new ArrayList<>(environ.lattice.get(value)));

        System.out.printf("%d %d\n", environ.lattice.size(), lattice.size());
    }

    public int getAllocid()
    {
        int res = allocid;
        allocid = 0;
        allocvalid = false;
        return res;
    }

    void copy(Environ e)
    {
        allocid = e.allocid;
        allocvalid = e.allocvalid;

        lattice.clear();
        for(Value value : e.lattice.keySet())
            lattice.put(value, new ArrayList<>(e.lattice.get(value)));

    }


    @Override
    public boolean equals(Object obj) {
        Environ en = (Environ) obj;
        for(Value value: lattice.keySet()) {
            if (!(en.lattice.containsKey(value))) return false;
        }
        if(en.lattice.size() != lattice.size()) return false;
        return true;
    }

}
