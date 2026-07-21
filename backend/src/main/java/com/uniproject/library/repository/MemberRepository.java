package com.uniproject.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.uniproject.library.model.Member;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
