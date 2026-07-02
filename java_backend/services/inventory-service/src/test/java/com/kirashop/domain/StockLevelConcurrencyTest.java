package com.kirashop.domain;

import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockLevelConcurrencyTest {

    @RepeatedTest(20)
    void reserve_underConcurrentLoad_neverOversells() throws InterruptedException {
        int initialStock = 50;
        int threadCount = 200;

        StockLevel stock = new StockLevel(1L, initialStock);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start= new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount= new AtomicInteger(0);

        for(int i=0; i< threadCount; i++){
            executorService.submit(() ->{
                ready.countDown();
                try{
                    start.await();
                    if(stock.reserve(1)){
                        successCount.incrementAndGet();
                    }
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(initialStock, successCount.get(),
        "Exacely 50 reservation should succeed out of 200 attempts.");
        assertEquals(0, stock.getAvailableQty());
    }
}
