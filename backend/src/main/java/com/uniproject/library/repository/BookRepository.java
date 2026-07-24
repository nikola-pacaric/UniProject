package com.uniproject.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.uniproject.library.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            SELECT b FROM Book b
            JOIN b.author a
            JOIN b.category c
            WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Book> search(@Param("q") String q);
}
