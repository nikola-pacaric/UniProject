package com.uniproject.library.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;


@Entity
@Table(name = "member")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "membership_card_number", nullable = false, length = 50, unique = true)
    private String membershipCardNumber;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    public Member() {super();}

    public Member(String firstName, String lastName, String membershipCardNumber, String email, String phone, Boolean active) {
        super();
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

    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }
    
}
