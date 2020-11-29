package test;

import benchmark.internal.*;
import benchmark.objects.*;

public class t1 {

    public static void main(String[] args)
    {
        BenchmarkN.alloc(1);
        A a = new A();
        BenchmarkN.alloc(2);
        A b = new A();

        for(int i = 0; i < 1; i++)
        {
            a = b;
        }
        BenchmarkN.alloc(3);
        B q = new B();
        b.f = q;

        BenchmarkN.alloc(4);
        A c = new A();
        c = a;
        BenchmarkN.test(1, c.f);
    }

}
