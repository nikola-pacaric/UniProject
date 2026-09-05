package com.example.uniproject.data.model.author;

public final class AuthorRequest {
    private final String firstName;
    private final String lastName;
    private final String biography;

    public AuthorRequest(String firstName, String lastName, String biography) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.biography = biography;
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
