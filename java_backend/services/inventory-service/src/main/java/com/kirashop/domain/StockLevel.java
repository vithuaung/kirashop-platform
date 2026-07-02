package com.kirashop.domain;

import java.util.concurrent.atomic.AtomicReference;

public class StockLevel {

    private Long productId;
    private final AtomicReference<StockCount> counts;

    public StockLevel(Long productId, int initialAvailable){
        this.productId = productId;
        this.counts = new AtomicReference<>(new StockCount(initialAvailable, 0));
    }

    public Boolean reserve(int qty) {
        while(true){
            StockCount current = counts.get();
            if(current.available() < qty){
                return false;
            }
            StockCount updated= new StockCount(current.available()-qty, current.reserve()+qty);
            if(counts.compareAndSet(current, updated)){
                return true;
            }
        }
    }

    public void release(int qty){
        counts.updateAndGet(c -> new StockCount(c.available()+qty, c.reserve()-qty));
    }

    public void commit(int qty){
        counts.updateAndGet(c -> new StockCount(c.available(), c.reserve()-qty));
    }

    public Long getProductId(){ return productId; }
    public int getAvailableQty(){ return counts.get().available(); }
    public int getReservedQty(){ return counts.get().reserve(); }
}
