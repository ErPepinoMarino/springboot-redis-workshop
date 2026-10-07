package com.redisworkshop.products;

public record BenchmarkResult(
        String mode, //cache o no
        int iterations, //numero de peticiones
        int concurrency, // lecturas simuyltaneas
        int count, // hits
        double totalMs, // total del test
        double avgMs, //estadisticas
        double minMs, //...
        double maxMs, //...
        double p95Ms, //percentil 95%
        double throughput) //lecturas por segundo. iterations/sec
         {
}