package com.example.uniproject.data.model.member;

public final class MemberRequest {
    private final String firstName;
    private final String lastName;
    private final String membershipCardNumber;
    private final String email;
    private final String phone;

    public MemberRequest(
            String firstName,
            String lastName,
            String membershipCardNumber,
            String email,
            String phone
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.membershipCardNumber = membershipCardNumber;
        this.email = email;
        this.phone = phone;
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
}
