package com.loanapi;


import com.loanapi.model.Loan;
import com.loanapi.model.LoanInstallment;
import com.loanapi.model.User;
import com.loanapi.repository.LoanInstallmentRepository;
import com.loanapi.services.LoanService;
import com.loanapi.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SpringBootTest
class LoanControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;

    private MockMvc mockMvc;

    @Value("#{'${loan.api.max.payable.months}'}")
    private Integer maxPayableMonths;
    @Value("#{'${loan.api.daily.penalty.interest.rate}'}")
    private BigDecimal dailyPenaltyRate;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Test
    void testLoans() throws Exception {
        User user = userService.getUser("customer");

        List<Loan> loans = loanService.getLoans(user.getUsername());

        for (Loan loan : loans) {
            System.out.printf("Loan Amount: %s, Installments: %d, isPaid: %b %n", loan.getLoanAmount().toString(), loan.getNumberOfInstallment(), loan.isPaid());
        }

    }

    @Test
    void testLoanDetails() throws Exception {
        User user = userService.getUser("customer");
        List<LoanInstallment> loanInstallments = loanInstallmentRepository.findLoanInstallmentsByLoanUserUsernameAndLoanId(user.getUsername(), 1l)
                .orElse(new ArrayList<>());

        for (LoanInstallment i : loanInstallments) {
            System.out.printf("Loan Installment ==> Amount: %s, Paid Amount: %s, Due Date: %s, Payment Date: %s %n", i.getAmount().toString(), i.getPaidAmount().toString(), sdf.format(i.getDueDate()), i.getPaymentDate() == null ? "-" : sdf.format(i.getPaymentDate()));
        }

    }

    @Test
    void testLoanToBePaid() throws Exception {
        List<LoanInstallment> loanInstallments = loanInstallmentRepository.findLoanInstallmentsByLoanUserUsernameAndLoanIdAndIsPaidFalse("customer", 1l)
                .orElseThrow(()->new Exception());
        Calendar nextPayableMonths = Calendar.getInstance();
        nextPayableMonths.add(Calendar.MONTH, maxPayableMonths);
        BigDecimal totalToBePaid = new BigDecimal(0);
        for(LoanInstallment loanInstallment : loanInstallments) {
            if(nextPayableMonths.getTime().before(loanInstallment.getDueDate())) {
                continue;
            }
            long daysBetween = ChronoUnit.DAYS.between(loanInstallment.getDueDate().toInstant(),
                    new Date().toInstant());

            BigDecimal penalty = loanInstallment.getAmount().multiply(dailyPenaltyRate.multiply(new BigDecimal(daysBetween)))
                    .setScale(2, RoundingMode.UP);
            loanInstallment.setPaidAmount(loanInstallment.getAmount().add(penalty));
            totalToBePaid = totalToBePaid.add(loanInstallment.getPaidAmount());
            System.out.println("Expected loanInstallment amount: " + loanInstallment.getPaidAmount());
        }
        System.out.println("Expected total amount to be paid: " + totalToBePaid);
    }
}
