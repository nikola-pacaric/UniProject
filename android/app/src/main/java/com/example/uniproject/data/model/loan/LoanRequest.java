package com.example.uniproject.data.model.loan;

public final class LoanRequest {
    private final Long bookId;
    private final Long memberId;

    public LoanRequest(Long bookId, Long memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
