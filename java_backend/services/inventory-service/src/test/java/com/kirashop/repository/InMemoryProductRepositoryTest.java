package com.kirashop.repository;

import com.kirashop.domain.NewProduct;
import com.kirashop.domain.Product;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryProductRepositoryTest {

    private NewProduct sample(String sku){
        return new NewProduct(sku,"Test Product","Test Category", BigDecimal.TEN);
    }

    @Test
    void save_newProduct_assignIdAndIsRetrievable(){
        InMemoryProductRepository repo = new InMemoryProductRepository();
        Product saved = repo.create(sample("SKH-1"));

        assertNotNull(saved.id());
        assertEquals(saved, repo.findById(saved.id()).orElseThrow());
    }

    @Test
    void save_existingProduct_replacesInPlace(){
        InMemoryProductRepository repo = new InMemoryProductRepository();
        Product created= repo.create(sample("SKU-2"));

        Product updated= created.withDetails("New Name", "New Category");
        repo.save(updated);

        Product fetched= repo.findById(updated.id()).orElseThrow();
        assertEquals("New Name", fetched.name());
        assertEquals("New Category", fetched.category());

    }

    @Test
    void create_duplicateSku_throws(){
        InMemoryProductRepository repo= new InMemoryProductRepository();
        repo.create(sample("SKU-3"));

        assertThrows(IllegalArgumentException.class,
                ()-> repo.create(sample("SKU-3")) );
    }

    @Test
    void findById_missing_returnEmpty(){
        InMemoryProductRepository repo = new InMemoryProductRepository();
        assertEquals(Optional.empty(), repo.findById(9999L));
    }

    @Test
    void deleteById_removesProductAndFreesSku(){
        InMemoryProductRepository repo = new InMemoryProductRepository();
        Product created = repo.create(sample("SKU-4"));

        repo.deleteById(created.id());

        assertTrue(repo.findById(created.id()).isEmpty());
        assertFalse(repo.existsBySku(created.sku()));
    }


    @Test
    void findAll_returnsEverythingCreated(){
        InMemoryProductRepository repo = new InMemoryProductRepository();
        repo.create(sample("SKU-5"));
        repo.create(sample("SKU-6"));
        repo.create(sample("SKU-7"));

        List<Product> all= repo.findAll();
        assertEquals(3, all.size());
    }

    @RepeatedTest(20)
    void create_duplicateSkuUnderConcurrentLoad_onlyOneEverSucceeds() throws InterruptedException {
        int threadCount=100;
        InMemoryProductRepository repo = new InMemoryProductRepository();
        ExecutorService executorService= Executors.newVirtualThreadPerTaskExecutor();

        CountDownLatch ready= new CountDownLatch(threadCount);
        CountDownLatch start= new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for(int i=0; i< threadCount; i++){
            executorService.submit(
                    ()->{
                        ready.countDown();
                        try{
                            start.await();
                            try{
                                repo.create(sample("SAME-SKU"));
                                successCount.incrementAndGet();
                            }catch (IllegalArgumentException expected){

                            }
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                        }finally {
                            done.countDown();
                        }
                    }
            );
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        executorService.shutdown();

        assertEquals(1, successCount.get(), "Exacely one save should succeed");
    }
}
