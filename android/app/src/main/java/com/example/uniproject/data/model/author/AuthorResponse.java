package com.example.uniproject.data.model.author;

public final class AuthorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String biography;

    public AuthorResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBiography() {
        return biography;
    }
}
