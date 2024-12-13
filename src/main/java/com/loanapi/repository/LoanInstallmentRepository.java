package com.loanapi.repository;

import com.loanapi.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {

    Optional<List<LoanInstallment>> findLoanInstallmentsByLoanUserUsernameAndLoanId(String username, Long id);

    Optional<List<LoanInstallment>> findLoanInstallmentsByLoanUserUsernameAndLoanIdAndIsPaidFalse(String username, Long id);
}
