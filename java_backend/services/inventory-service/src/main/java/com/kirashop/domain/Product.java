package com.kirashop.domain;

import java.math.BigDecimal;

public record Product(Long id, String sku, String name, String category, BigDecimal price) {

    public Product{
        if(id == null) throw new IllegalArgumentException("Id must not be null");
        if(sku == null || sku.isBlank()) throw new IllegalArgumentException("Sku must not be blanked or null");
        if(name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be null or blanked");
        if(category == null || category.isBlank()) throw new IllegalArgumentException("Category must not be null or blanked");
        if(price == null || price.compareTo(BigDecimal.ZERO) <0) throw new IllegalArgumentException("Price must not be lesser than zero");
    }

    public Product withDetails(String newName, String newCategory){
        return new Product(id, sku, newName, newCategory, price);
    }

    public Product withPrice(BigDecimal newPrice){
        return new Product(id, sku, name, category, newPrice);
    }
}


