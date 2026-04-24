package com.bank.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;

    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {
        if (version == null) {
            version = 0L;
        }
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}