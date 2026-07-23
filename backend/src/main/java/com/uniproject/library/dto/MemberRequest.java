package com.uniproject.library.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Membership card number is required")
    @Size(max = 50)
    private String membershipCardNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150)
    private String email;

    @Size(max = 50)
    private String phone;

    public MemberRequest() {super();}

    public MemberRequest(String firstName, String lastName, String membershipCardNumber, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.membershipCardNumber = membershipCardNumber;
        this.email = email;
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMembershipCardNumber() {
        return membershipCardNumber;
    }

    public void setMembershipCardNumber(String membershipCardNumber) {
        this.membershipCardNumber = membershipCardNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    
}
