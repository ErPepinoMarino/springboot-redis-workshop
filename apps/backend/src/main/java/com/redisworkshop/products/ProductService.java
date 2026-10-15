package com.redisworkshop.products;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private static final int SEARCH_LIMIT = 100;

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

    @Cacheable(value = "products", key = "'search:' + #q.toLowerCase()")
    public List<Product> search(String q) {
        return searchNoCache(q);
    }

    public List<Product> searchNoCache(String q) {
        return repository.searchByName(q, PageRequest.of(0, SEARCH_LIMIT));
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
