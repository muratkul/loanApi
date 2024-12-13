package com.loanapi.services;

import com.loanapi.model.LoanApiException;
import com.loanapi.model.Loan;
import com.loanapi.model.LoanInstallment;
import com.loanapi.payload.LoanRequest;
import com.loanapi.payload.PayLoanRequest;
import com.loanapi.payload.PayLoanResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface LoanService {

    List<Loan> getLoans(Authentication authentication) throws LoanApiException;

    List<Loan> getLoans(String username) throws LoanApiException;

    List<LoanInstallment> getLoanInstallments(Authentication authentication, Long id) throws LoanApiException;

    List<Loan> getAllLoans(Authentication authentication);

    void createLoan(Authentication authentication, LoanRequest loanRequest) throws LoanApiException;

    PayLoanResponse payLoanInstallment(Authentication authentication, PayLoanRequest payLoanRequest) throws LoanApiException;
}
