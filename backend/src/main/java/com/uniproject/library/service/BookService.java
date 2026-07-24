package com.uniproject.library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.uniproject.library.repository.BookRepository;
import com.uniproject.library.dto.BookResponse;
import com.uniproject.library.exception.BadRequestException;
import com.uniproject.library.exception.ResourceNotFoundException;
import com.uniproject.library.model.Book;
import com.uniproject.library.repository.AuthorRepository;
import com.uniproject.library.repository.CategoryRepository;
import com.uniproject.library.repository.LoanRepository;
import com.uniproject.library.dto.BookRequest;
import com.uniproject.library.model.Author;
import com.uniproject.library.model.Category;
import com.uniproject.library.model.LoanStatus;

@Service
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }
        return bookRepository.search(q.trim()).stream().map(this::toResponse).toList();
    }

    public BookResponse create(BookRequest request) {
        Author author = authorRepository.findById(request.getAuthorId()).orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setTotalCopies(request.getTotalCopies());
        // Business rule: on creation, availableCopies should be equal to totalCopies
        book.setAvailableCopies(request.getTotalCopies());
        book.setAuthor(author);
        book.setCategory(category);

        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        Author author = authorRepository.findById(request.getAuthorId()).orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        int copiesOnLoan = book.getTotalCopies() - book.getAvailableCopies();
        int newTotal = request.getTotalCopies();

        if (newTotal < copiesOnLoan) {
            throw new BadRequestException("Cannot set total copies to " + newTotal
            + " because " + copiesOnLoan + " copies are currently on loan");
        }

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setTotalCopies(newTotal);
        book.setAuthor(author);
        book.setCategory(category);
        book.setAvailableCopies(newTotal - copiesOnLoan);

        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        if (loanRepository.existsByBookIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new BadRequestException("Cannot delete book with id " + id + " because it has active loans");
        }
        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
            book.getId(), 
            book.getTitle(), 
            book.getIsbn(), 
            book.getPublicationYear(),
            book.getTotalCopies(),
            book.getAvailableCopies(),
            book.getAuthor().getId(), 
            book.getCategory().getId()
        );
    }
}
