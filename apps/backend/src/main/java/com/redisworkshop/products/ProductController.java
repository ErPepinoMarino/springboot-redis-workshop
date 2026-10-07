package com.redisworkshop.products;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache/products")
public class ProductController {

    private final ProductService service;
    private final BenchmarkService benchmarkService;

    public ProductController(ProductService service, BenchmarkService benchmarkService) {
        this.service = service;
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id,
                                           @RequestParam(defaultValue = "true") boolean cached) {
        Product product = cached ? service.findById(id) : service.findByIdNoCache(id);
        return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> evict(@PathVariable Long id) {
        service.evict(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bench")
    public BenchmarkResult bench(@RequestParam(defaultValue = "1000") int iterations,
                                 @RequestParam(defaultValue = "true") boolean cached,
                                 @RequestParam(defaultValue = "16") int concurrency) {
        int safeIterations = Math.max(1, Math.min(iterations, 50000));
        int safeConcurrency = Math.max(1, Math.min(concurrency, 200));
        return benchmarkService.run(safeIterations, cached, safeConcurrency);
    }
    @DeleteMapping
    public ResponseEntity<Void> clear() {
        service.clear();
        return ResponseEntity.noContent().build();
    }
}