package com.uniproject.library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniproject.library.dto.LoanRequest;
import com.uniproject.library.dto.LoanResponse;
import com.uniproject.library.exception.ResourceNotFoundException;
import com.uniproject.library.exception.BadRequestException;
import com.uniproject.library.model.Loan;
import com.uniproject.library.model.LoanStatus;
import com.uniproject.library.repository.BookRepository;
import com.uniproject.library.repository.LoanRepository;
import com.uniproject.library.repository.MemberRepository;
import com.uniproject.library.model.Member;
import com.uniproject.library.model.Book;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        return toResponse(getLoanOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findOverdue() {
        return loanRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDate.now()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        return loanRepository.findByMemberId(memberId).stream().map(this::toResponse).toList();
    }

    public LoanResponse borrow(LoanRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (Boolean.FALSE.equals(member.getActive())) {
            throw new BadRequestException("Cannot loan to inactive member with id: " + member.getId());
        }

        Book book = bookRepository.findById(request.getBookId()).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No available copies for book with id: " + book.getId());
        }

        LocalDate today = LocalDate.now();

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(LOAN_PERIOD_DAYS));
        loan.setReturnDate(null);
        loan.setStatus(LoanStatus.ACTIVE);

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        bookRepository.save(book);
        Loan saved = loanRepository.save(loan);

        return toResponse(saved);
    }

    public LoanResponse returnLoan(Long loanId) {
        Loan loan = getLoanOrThrow(loanId);

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestException("Loan with id " + loanId + " is already returned.");
        }

        Book book = loan.getBook();

        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new BadRequestException("Cannot return loan: available copies already at total for book id " + book.getId());
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        bookRepository.save(book);
        Loan saved = loanRepository.save(loan);

        return toResponse(saved);
    }

    private Loan getLoanOrThrow(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
            loan.getId(),
            loan.getBook().getId(),
            loan.getMember().getId(),
            loan.getLoanDate(),
            loan.getDueDate(),
            loan.getReturnDate(),
            loan.getStatus()
        );
    }
}
