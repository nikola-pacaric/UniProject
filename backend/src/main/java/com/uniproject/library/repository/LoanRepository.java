package com.uniproject.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uniproject.library.model.Loan;
import com.uniproject.library.model.LoanStatus;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    boolean existsByBookIdAndStatus(Long bookId, LoanStatus status);

    //Overdue
    List<Loan> findByDueDateBeforeAndReturnDateIsNull(LocalDate date);
}
