package com.loanapi.payload;


import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class PayLoanRequest {

    @Size(min = 0, max = 50)
    private String username;

    private Long loanId;

    private BigDecimal amount;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
