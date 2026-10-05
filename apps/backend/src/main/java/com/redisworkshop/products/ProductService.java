package com.redisworkshop.products;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    @Cacheable("products")
        public Product findById(Long id) {
            return repository.findById(id).orElse(null);
        }
}
