package com.loanapi.controller;

import com.loanapi.Translator;
import com.loanapi.model.LoanApiException;
import com.loanapi.payload.LoanRequest;
import com.loanapi.payload.MessageResponse;
import com.loanapi.payload.PayLoanRequest;
import com.loanapi.payload.PayLoanResponse;
import com.loanapi.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/loans")
@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
public class LoanController {

    @Autowired
    private LoanService loanService;

    private static final String NOT_LOGGED_IN = "error.code.NOT_LOGGED_IN";

    @GetMapping("/get")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> getLoans(Authentication authentication) throws LoanApiException {

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(loanService.getLoans(authentication));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(Translator.toLocale(NOT_LOGGED_IN)));
        }
    }

    @GetMapping("/get/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLoans(Authentication authentication, @PathVariable String username) throws LoanApiException {

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(loanService.getLoans(username));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(Translator.toLocale(NOT_LOGGED_IN)));
        }
    }

    @GetMapping("/getInstallments/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> getLoanInstallments(Authentication authentication, @PathVariable Long id) throws LoanApiException {

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(loanService.getLoanInstallments(authentication, id));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(Translator.toLocale(NOT_LOGGED_IN)));
        }
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllLoans(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(loanService.getAllLoans(authentication));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(Translator.toLocale(NOT_LOGGED_IN)));
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> createLoan(Authentication authentication, @Validated @RequestBody LoanRequest loanRequest) {

        try {
            loanService.createLoan(authentication, loanRequest);

        } catch (LoanApiException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getDescription()));
        }

        return ResponseEntity.ok().body(new MessageResponse(Translator.toLocale("info.code.loan.created")));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> pay(Authentication authentication, @Validated @RequestBody PayLoanRequest payLoanRequest) {

        PayLoanResponse response;
        try {
            response = loanService.payLoanInstallment(authentication, payLoanRequest);
        } catch (LoanApiException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getDescription()));
        }

        return ResponseEntity.ok().body(response);
    }

}
