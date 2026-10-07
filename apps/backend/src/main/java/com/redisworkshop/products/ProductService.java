package com.redisworkshop.products;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

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
    
    public List<Long> ids() {
        return repository.findAllIds();
    }

    @CacheEvict("products")
    public void evict(Long id) {
    }

    @CacheEvict(value = "products", allEntries = true)
    public void clear() {
    }

    private Product load(Long id) {
        return repository.findById(id).orElse(null);
    }
}
