package com.example.uniproject.data.model.book;

public final class BookRequest {
    private final String title;
    private final String isbn;
    private final Integer publicationYear;
    private final Integer totalCopies;
    private final Long authorId;
    private final Long categoryId;

    public BookRequest(
            String title,
            String isbn,
            Integer publicationYear,
            Integer totalCopies,
            Long authorId,
            Long categoryId
    ) {
        this.title = title;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.authorId = authorId;
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }
}
