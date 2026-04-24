package com.bank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.api.entity.IdempotencyKey;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {
}