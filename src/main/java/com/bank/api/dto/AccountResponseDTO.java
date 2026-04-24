package com.bank.api.dto;

import java.math.BigDecimal;

public class AccountResponseDTO {

    private Long id;
    private String owner;
    private BigDecimal balance;

    public AccountResponseDTO(Long id, String owner, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
}