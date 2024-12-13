package com.loanapi.payload;

import java.math.BigDecimal;

public class PayLoanResponse {

    private int installmentsPaid = 0;
    private BigDecimal totalAmountPaid = new BigDecimal(0);
    private boolean isCompletelyPaid = false;

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
    }

    public BigDecimal getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public boolean isCompletelyPaid() {
        return isCompletelyPaid;
    }

    public void setCompletelyPaid(boolean completelyPaid) {
        isCompletelyPaid = completelyPaid;
    }
}
