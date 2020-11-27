package test;

import benchmark.internal.Benchmark;
import benchmark.internal.BenchmarkN;
import benchmark.objects.*;

public class ssa {
    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        A a = new A();
        BenchmarkN.alloc(2);
        A b = new A();
        BenchmarkN.alloc(3);
        A c = new A();
        BenchmarkN.test(1, a);
        BenchmarkN.test(2, c);

        c = a;
        BenchmarkN.test(3, c);

    }
}
