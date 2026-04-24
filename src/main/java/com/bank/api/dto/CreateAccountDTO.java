package com.bank.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateAccountDTO {

    @NotBlank
    private String owner;

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}