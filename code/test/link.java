package test;

import benchmark.internal.*;
import benchmark.objects.*;

public class link {
    public static void main(String[] args)
    {
        BenchmarkN.alloc(1);
        N a = new N();
        BenchmarkN.alloc(2);
        N b = new N();
        a = b;
        a.next = b;
        BenchmarkN.test(1, a);
        BenchmarkN.test(2, a.next);
    }

}
