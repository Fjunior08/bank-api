package com.bank.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.api.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountIdOrToAccountId(Long from, Long to);
}