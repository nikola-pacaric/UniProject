package com.uniproject.library.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniproject.library.dto.AuthResponse;
import com.uniproject.library.dto.LoginRequest;
import com.uniproject.library.dto.RegisterRequest;
import com.uniproject.library.model.Librarian;
import com.uniproject.library.repository.LibrarianRepository;
import com.uniproject.library.exception.BadRequestException;
import com.uniproject.library.exception.UnauthorizedException;
import com.uniproject.library.security.JwtService;

@Service
public class AuthService {

    private final LibrarianRepository librarianRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(LibrarianRepository librarianRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.librarianRepository = librarianRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (librarianRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (librarianRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        Librarian librarian = new Librarian(
            request.getUsername(),
            request.getEmail(),
            hash,
            request.getFullName()
        );
        librarianRepository.save(librarian);

        return new AuthResponse(
            null,
            librarian.getUsername(),
            librarian.getFullName(),
            "Registration successful"
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Librarian librarian = librarianRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), librarian.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtService.generateToken(librarian.getUsername());

        return new AuthResponse(
            token,
            librarian.getUsername(),
            librarian.getFullName(),
            "Login successful"
        );
    }

}
