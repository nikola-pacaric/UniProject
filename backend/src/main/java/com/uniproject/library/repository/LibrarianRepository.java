package com.uniproject.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uniproject.library.model.Librarian;

@Repository
public interface LibrarianRepository extends JpaRepository<Librarian, Long> {

    Optional<Librarian> findByUsername(String username);
    Optional<Librarian> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
