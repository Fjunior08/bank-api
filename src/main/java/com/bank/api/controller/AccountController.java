package com.bank.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.bank.api.dto.*;
import com.bank.api.entity.Transaction;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService service;
    private final TransactionRepository transactionRepository;

    public AccountController(AccountService service, TransactionRepository transactionRepository) {
        this.service = service;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ApiResponse<AccountResponseDTO> create(@RequestBody @Valid CreateAccountDTO dto) {
        return ApiResponse.success(service.create(dto.getOwner()));
    }

    @PostMapping("/{id}/deposit")
    public ApiResponse<Void> deposit(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {

        service.deposit(id, amount, key);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {

        service.withdraw(id, amount, key);
        return ApiResponse.success(null);
    }

    @PostMapping("/transfer")
    public ApiResponse<Void> transfer(
            @RequestBody @Valid TransferDTO dto,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {

        service.transfer(dto.getFrom(), dto.getTo(), dto.getAmount(), key);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/statement")
    public ApiResponse<List<Transaction>> statement(@PathVariable Long id) {
        return ApiResponse.success(
                transactionRepository.findByFromAccountIdOrToAccountId(id, id)
        );
    }
}