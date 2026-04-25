package com.bank.api;

import com.bank.api.entity.Account;
import com.bank.api.repository.AccountRepository;
import com.bank.api.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AccountConcurrencyTest {

    @Autowired
    private AccountService service;

    @Autowired
    private AccountRepository repository;

    @Test
    void shouldHandleConcurrentWithdrawCorrectly() throws InterruptedException {

        Account acc = new Account();
        acc.setOwner("Franciss");
        acc.setBalance(new BigDecimal("100"));

        acc = repository.save(acc);

        final Long accountId = acc.getId();

        int threads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    service.withdraw(accountId, new BigDecimal("100"), null);

                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Account updated = repository.findById(accountId).orElseThrow();

        System.out.println("Saldo final: " + updated.getBalance());
        System.out.println("Success: " + success.get());
        System.out.println("Fail: " + fail.get());

        assertEquals(1, success.get());
        assertEquals(1, fail.get());
        assertEquals(0, updated.getBalance().compareTo(BigDecimal.ZERO));
    }
}