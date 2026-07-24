package com.uniproject.library.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uniproject.library.service.LoanService;
import com.uniproject.library.service.MemberService;
import com.uniproject.library.dto.MemberResponse;
import com.uniproject.library.dto.MemberRequest;
import com.uniproject.library.dto.LoanResponse;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;

    public MemberController(MemberService memberService, LoanService loanService) {
        this.memberService = memberService;
        this.loanService = loanService;
    }

    @GetMapping
    public List<MemberResponse> getAll() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @GetMapping("/{id}/loans")
    public List<LoanResponse> getLoans(@PathVariable Long id) {
        return loanService.findByMemberId(id);
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        MemberResponse created = memberService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return memberService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public MemberResponse deactivate(@PathVariable Long id) {
        return memberService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    public MemberResponse activate(@PathVariable Long id) {
        return memberService.activate(id);
    }
}
