package com.uniproject.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20)
    private String isbn;

    @NotNull(message = "Publication year is required")
    private Integer publicationYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 0, message = "Total copies must be >= 0")
    private Integer totalCopies;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    public BookRequest() {super();}

    public BookRequest(String title, String isbn, Integer publicationYear, Integer totalCopies, Long authorId, Long categoryId) {
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    
}
