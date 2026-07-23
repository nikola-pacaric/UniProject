package com.uniproject.library.dto;

public class MemberResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String membershipCardNumber;
    private String email;
    private String phone;
    private Boolean active;

    public MemberResponse() {super();}

    public MemberResponse(Long id, String firstName, String lastName, String membershipCardNumber, String email, String phone, Boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.membershipCardNumber = membershipCardNumber;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    
}
