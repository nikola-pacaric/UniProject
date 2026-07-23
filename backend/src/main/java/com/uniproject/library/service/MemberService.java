package com.uniproject.library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniproject.library.repository.MemberRepository;
import com.uniproject.library.dto.MemberResponse;
import com.uniproject.library.exception.ResourceNotFoundException;
import com.uniproject.library.model.Member;
import com.uniproject.library.dto.MemberRequest;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        return toResponse(member);
    }

    public MemberResponse create(MemberRequest request) {
        Member member = new Member();

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setMembershipCardNumber(request.getMembershipCardNumber());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setActive(true);

        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    public MemberResponse update(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setMembershipCardNumber(request.getMembershipCardNumber());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());

        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    public MemberResponse deactivate(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        if (Boolean.FALSE.equals(member.getActive())) {
            return toResponse(member);
        }
        
        member.setActive(false);
        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getFirstName(),
            member.getLastName(),
            member.getMembershipCardNumber(),
            member.getEmail(),
            member.getPhone(),
            member.getActive()
        );
    }
}
