package com.uniproject.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.uniproject.library.model.Author;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
