package com.kirashop.domain;

public record StockCount(int available, int reserve) {
    public StockCount{
        if(available < 0) throw new IllegalArgumentException("Available cannot be lesser than zero: "+ available);
        if(reserve < 0) throw new IllegalArgumentException("Reserve cannot be lesser than zero:" + reserve);
    }
}
