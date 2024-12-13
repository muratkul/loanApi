package com.loanapi.services.impl;

import com.loanapi.model.*;
import com.loanapi.payload.LoanRequest;
import com.loanapi.payload.PayLoanRequest;
import com.loanapi.payload.PayLoanResponse;
import com.loanapi.repository.LoanInstallmentRepository;
import com.loanapi.repository.LoanRepository;
import com.loanapi.repository.UserRepository;
import com.loanapi.services.LoanService;
import com.loanapi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;

    @Value("#{'${loan.api.installment.options}'.split(',')}")
    private List<Integer> installmentOptions;

    @Value("#{'${loan.api.max.interest.rate}'}")
    private BigDecimal maxInterestRate;

    @Value("#{'${loan.api.min.interest.rate}'}")
    private BigDecimal minInterestRate;

    @Value("#{'${loan.api.max.payable.months}'}")
    private Integer maxPayableMonths;

    @Value("#{'${loan.api.daily.penalty.interest.rate}'}")
    private BigDecimal dailyPenaltyRate;


    public List<Loan> getLoans(Authentication authentication) throws LoanApiException {

        User user = userService.getUser(authentication);
        return loanRepository.findLoansByUserUsername(user.getUsername())
                .orElseGet(() -> new ArrayList<>());
    }

    public List<Loan> getLoans(String username) throws LoanApiException {

        User user = userService.getUser(username);
        return loanRepository.findLoansByUserUsername(user.getUsername())
                .orElseGet(() -> new ArrayList<>());
    }

    public List<LoanInstallment> getLoanInstallments(Authentication authentication, Long id) throws LoanApiException {

        User user = userService.getUser(authentication);
        return loanInstallmentRepository.findLoanInstallmentsByLoanUserUsernameAndLoanId(user.getUsername(), id)
                .orElseGet(() -> new ArrayList<>());
    }

    public List<Loan> getAllLoans(Authentication authentication) {

        return loanRepository.findAll();
    }

    @Transactional
    public void createLoan(Authentication authentication, LoanRequest loanRequest) throws LoanApiException {

        User user = userService.getUser(authentication);

        //If any username entered, proceed for customer
        if (userService.isAdmin(user) && !StringUtils.isEmpty(loanRequest.getUsername())) {
            user = userService.getUser(loanRequest.getUsername());
        }

        BigDecimal totalCreditLimit = user.getCreditLimit();
        BigDecimal usedCreditLimit = user.getUsedCreditLimit();
        BigDecimal availableCreditLimit = totalCreditLimit.subtract(usedCreditLimit);
        Integer installments = loanRequest.getInstallments();

        if (loanRequest.getAmount().compareTo(new BigDecimal(0)) != 1) {
            throw new LoanApiException(LoanApiErrorCode.WRONG_AMOUNT);
        }

        if (availableCreditLimit.compareTo(loanRequest.getAmount()) == -1) {
            throw new LoanApiException(LoanApiErrorCode.CREDIT_LIMIT_EXCEEDED);
        }

        if (maxInterestRate.compareTo(loanRequest.getInterestRate()) == -1) {
            throw new LoanApiException(LoanApiErrorCode.INTEREST_RATE_EXCEEDED);
        }

        if (minInterestRate.compareTo(loanRequest.getInterestRate()) == 1) {
            throw new LoanApiException(LoanApiErrorCode.INTEREST_RATE_EXCEEDED);
        }

        if (!installmentOptions.stream().filter(x->x.equals(installments)).findAny().isPresent()) {
            throw new LoanApiException(LoanApiErrorCode.INVALID_INSTALLMENT_OPTION);
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanAmount(loanRequest.getAmount());
        loan.setPaid(false);
        loan.setNumberOfInstallment(loanRequest.getInstallments());
        loan.setCreateDate(new Date());
        loan = loanRepository.save(loan);

        BigDecimal totalAmount = loan.getLoanAmount().multiply(loanRequest.getInterestRate().add(new BigDecimal(1)));
        BigDecimal installmentAmount = totalAmount.divide(new BigDecimal(loan.getNumberOfInstallment()), 2, RoundingMode.UP);
        BigDecimal lastInstallmentAmount = totalAmount.subtract(installmentAmount.multiply(new BigDecimal(loan.getNumberOfInstallment()-1)));
        Calendar nextDueDate = Calendar.getInstance();
        nextDueDate.add(Calendar.MONTH, 1);
        nextDueDate.set(Calendar.DAY_OF_MONTH, 1);

        for (int i = 1 ; i <= loan.getNumberOfInstallment(); i++) {
            LoanInstallment loanInstallment;
            if (i == loan.getNumberOfInstallment()) {
                loanInstallment = new LoanInstallment(loan, lastInstallmentAmount, new BigDecimal(0), nextDueDate.getTime(), null, false);
            } else {
                loanInstallment = new LoanInstallment(loan, installmentAmount, new BigDecimal(0), nextDueDate.getTime(), null, false);
            }
            loanInstallmentRepository.save(loanInstallment);
            nextDueDate.add(Calendar.MONTH, 1);
        }

        user.setUsedCreditLimit(usedCreditLimit.add(loanRequest.getAmount()));
        userRepository.save(user);
    }

    @Transactional
    public PayLoanResponse payLoanInstallment(Authentication authentication, PayLoanRequest payLoanRequest) throws LoanApiException {

        PayLoanResponse payLoanResponse = new PayLoanResponse();

        User user = userService.getUser(authentication);

        //If any username entered, proceed for customer
        if (userService.isAdmin(user) && !StringUtils.isEmpty(payLoanRequest.getUsername())) {
            user = userService.getUser(payLoanRequest.getUsername());
        }

        Calendar nextPayableMonths = Calendar.getInstance();
        nextPayableMonths.add(Calendar.MONTH, maxPayableMonths);

        List<LoanInstallment> loanInstallments = loanInstallmentRepository.findLoanInstallmentsByLoanUserUsernameAndLoanIdAndIsPaidFalse(user.getUsername(),
                payLoanRequest.getLoanId()).orElseThrow(() -> new LoanApiException(LoanApiErrorCode.NO_LOAN_INSTALLMENTS_FOUND));

        BigDecimal payRequestAmount = payLoanRequest.getAmount();
        int installmentsPaidCount = 0;
        BigDecimal paidAmount = new BigDecimal(0);
        for(LoanInstallment loanInstallment : loanInstallments) {
            if(nextPayableMonths.getTime().before(loanInstallment.getDueDate())) {
                continue;
            }

            //If the due date is expired value is positive, otherwise negative
            long daysBetween = ChronoUnit.DAYS.between(loanInstallment.getDueDate().toInstant(),
                    new Date().toInstant());

            //Negative penalty is early payment reward
            BigDecimal penalty = loanInstallment.getAmount().multiply(dailyPenaltyRate.multiply(new BigDecimal(daysBetween)))
                    .setScale(2, RoundingMode.UP);

            BigDecimal notPaidAmount = loanInstallment.getAmount().add(penalty).subtract(loanInstallment.getPaidAmount());
            if(payRequestAmount.compareTo(notPaidAmount) >= 0) {
                loanInstallment.setPaidAmount(loanInstallment.getAmount().add(penalty));
                loanInstallment.setPaid(true);
                loanInstallment.setPaymentDate(new Date());
                loanInstallmentRepository.save(loanInstallment);
                payRequestAmount = payRequestAmount.subtract(notPaidAmount);
                paidAmount = paidAmount.add(notPaidAmount);
                installmentsPaidCount++;
            } else {
                break;
            }
        }
        boolean isCompleted = !loanInstallments.stream().filter(x -> !x.isPaid()).findAny().isPresent();

        if(isCompleted) {
            Loan loan = loanRepository.findLoanByUserUsernameAndId(user.getUsername(), payLoanRequest.getLoanId())
                    .orElseThrow(() -> new LoanApiException(LoanApiErrorCode.LOAN_NOT_FOUND));

            loan.setPaid(true);
            loanRepository.save(loan);
            user.setUsedCreditLimit(user.getUsedCreditLimit().subtract(loan.getLoanAmount()));
            userRepository.save(user);
        }

        payLoanResponse.setInstallmentsPaid(installmentsPaidCount);
        payLoanResponse.setTotalAmountPaid(paidAmount);
        payLoanResponse.setCompletelyPaid(isCompleted);

        return payLoanResponse;
    }

}
