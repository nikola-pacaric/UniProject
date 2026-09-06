package com.example.uniproject.data.model.loan;

public final class LoanResponse {
    private Long id;
    private Long bookId;
    private String bookTitleAtLoan;
    private Long memberId;
    private String loanDate;
    private String dueDate;
    private String returnDate;
    private String status;

    public LoanResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitleAtLoan() {
        return bookTitleAtLoan;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getLoanDate() {
        return loanDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }
}
