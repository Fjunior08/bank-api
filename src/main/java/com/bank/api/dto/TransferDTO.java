package com.bank.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public class TransferDTO {

    @NotNull
    private Long from;

    @NotNull
    private Long to;

    @NotNull
    private BigDecimal amount;

    public Long getFrom() { return from; }
    public void setFrom(Long from) { this.from = from; }

    public Long getTo() { return to; }
    public void setTo(Long to) { this.to = to; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}