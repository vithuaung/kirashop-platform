package com.kirashop.repository;

import com.kirashop.domain.NewProduct;
import com.kirashop.domain.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryProductRepository implements ProductRepository{

    private final AtomicLong id = new AtomicLong(1);
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Boolean> skuIndex = new ConcurrentHashMap<>();

    @Override
    public Product create(NewProduct draft){
        if(skuIndex.putIfAbsent(draft.sku(),Boolean.TRUE) !=null){
            // sku is existed, cannot proceed
            throw new IllegalArgumentException("sku already existed, not allowed to proceed");
        }
        Long newId = generateId();
        Product newProduct = new Product(newId, draft.sku(),draft.name(), draft.category(), draft.price());
        products.put(newProduct.id(), newProduct);
        return newProduct;
    }

    @Override
    public Product save(Product product) {
        products.replace(product.id(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return products.values().stream().toList();
    }

    @Override
    public boolean existsBySku(String sku) {
        return skuIndex.containsKey(sku);
    }

    @Override
    public void deleteById(Long id) {
        Product removed = products.remove(id);
        if(removed !=null)
            skuIndex.remove(removed.sku());
    }

    private Long generateId(){
        return id.incrementAndGet();
    }
}
