package com.redisworkshop.products;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p.id from Product p")
    List<Long> findAllIds();

    @Query("select p from Product p "
            + "where lower(p.name) like lower(concat('%', :q, '%')) "
            + "order by p.name")
    List<Product> searchByName(@Param("q") String q, Pageable pageable);
}