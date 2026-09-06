package com.example.uniproject.data.model.member;

public final class MemberResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String membershipCardNumber;
    private String email;
    private String phone;
    private Boolean active;

    public MemberResponse() {
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

    public String getMembershipCardNumber() {
        return membershipCardNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Boolean getActive() {
        return active;
    }
}
