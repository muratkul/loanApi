package com.loanapi.repository;

import com.loanapi.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<List<Loan>> findLoansByUserUsername(String username);

    Optional<Loan> findLoanByUserUsernameAndId(String username, Long id);
}
