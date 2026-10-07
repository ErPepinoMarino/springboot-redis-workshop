package com.redisworkshop.products;

import org.springframework.cache.annotation.CacheEvict;
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
            return load(id);
        }
    
    public Product findByIdNoCache(Long id) {
        return load(id);
    }

    @CacheEvict("products")
    public void evict(Long id) {
    }

    private Product load(Long id) {
        return repository.findById(id).orElse(null);
    }
}
