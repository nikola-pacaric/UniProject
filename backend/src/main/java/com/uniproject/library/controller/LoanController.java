package com.uniproject.library.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uniproject.library.service.LoanService;
import java.util.List;

import com.uniproject.library.dto.LoanRequest;
import com.uniproject.library.dto.LoanResponse;
import com.uniproject.library.service.LoanService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.findAll();
    }

    @GetMapping("/overdue")
    public List<LoanResponse> getOverdue() {
        return loanService.findOverdue();
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Long id) {
        return loanService.findById(id);
    }

    @PostMapping
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody LoanRequest request) {
        LoanResponse created = loanService.borrow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }
}
