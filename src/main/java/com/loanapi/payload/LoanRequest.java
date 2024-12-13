package com.loanapi.payload;


import javax.validation.constraints.*;
import java.math.BigDecimal;

public class LoanRequest {

    @Size(min = 0, max = 50)
    private String username;

    private int installments;

    private BigDecimal interestRate;

    private BigDecimal amount;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getInstallments() {
        return installments;
    }

    public void setInstallments(int installments) {
        this.installments = installments;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
