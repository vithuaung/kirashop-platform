package com.kirashop.repository;

import com.kirashop.domain.NewProduct;
import com.kirashop.domain.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product create(NewProduct newProduct);
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    boolean existsBySku(String sku);
    void deleteById(Long id);
}
