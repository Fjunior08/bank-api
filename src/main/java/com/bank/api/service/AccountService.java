package com.bank.api.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.api.dto.AccountResponseDTO;
import com.bank.api.entity.Account;
import com.bank.api.entity.Transaction;
import com.bank.api.entity.IdempotencyKey;
import com.bank.api.exception.BusinessException;
import com.bank.api.repository.AccountRepository;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.repository.IdempotencyRepository;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRepository idempotencyRepository;

    public AccountService(AccountRepository repository,
                          TransactionRepository transactionRepository,
                          IdempotencyRepository idempotencyRepository) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
    }

    @Transactional
    public AccountResponseDTO create(String owner) {
        Account account = new Account();
        account.setOwner(owner);

        Account saved = repository.save(account);

        return new AccountResponseDTO(
                saved.getId(),
                saved.getOwner(),
                saved.getBalance()
        );
    }

    private void validateIdempotency(String key, String endpoint) {
        if (key == null || key.isBlank()) return;

        if (idempotencyRepository.existsById(key)) {
            log.warn("Requisição duplicada detectada key={} endpoint={}", key, endpoint);
            throw new BusinessException("Requisição duplicada");
        }

        IdempotencyKey entity = new IdempotencyKey();
        entity.setId(key);
        entity.setEndpoint(endpoint);

        idempotencyRepository.save(entity);
    }

    @Transactional
    public void deposit(Long id, BigDecimal amount, String idempotencyKey) {

        validateIdempotency(idempotencyKey, "deposit");

        Account account = repository.findByIdForUpdate(id);

        if (account == null) {
            throw new BusinessException("Conta não encontrada");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor inválido");
        }

        account.setBalance(account.getBalance().add(amount));

        Transaction t = new Transaction();
        t.setToAccountId(id);
        t.setAmount(amount);
        t.setType("DEPOSIT");

        transactionRepository.save(t);

        log.info("Depósito realizado accountId={} amount={}", id, amount);
    }

    @Transactional
    public void withdraw(Long id, BigDecimal amount, String idempotencyKey) {

        validateIdempotency(idempotencyKey, "withdraw");

        Account account = repository.findByIdForUpdate(id);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction t = new Transaction();
        t.setFromAccountId(id);
        t.setAmount(amount);
        t.setType("WITHDRAW");

        transactionRepository.save(t);

        log.info("Saque realizado accountId={} amount={}", id, amount);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, String idempotencyKey) {

        validateIdempotency(idempotencyKey, "transfer");

        Account from = repository.findByIdForUpdate(fromId);
        Account to = repository.findByIdForUpdate(toId);

        if (from == null || to == null) {
            throw new BusinessException("Conta não encontrada");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction t = new Transaction();
        t.setFromAccountId(fromId);
        t.setToAccountId(toId);
        t.setAmount(amount);
        t.setType("TRANSFER");

        transactionRepository.save(t);

        log.info("Transferência realizada from={} to={} amount={}", fromId, toId, amount);
    }
}