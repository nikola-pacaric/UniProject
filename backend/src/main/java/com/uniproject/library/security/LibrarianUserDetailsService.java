package com.uniproject.library.security;

import com.uniproject.library.model.Librarian;
import com.uniproject.library.repository.LibrarianRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class LibrarianUserDetailsService implements UserDetailsService {

    private final LibrarianRepository librarianRepository;

    public LibrarianUserDetailsService(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Librarian librarian = librarianRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Librarian not found with username: " + username));

        return User.withUsername(librarian.getUsername()).password(librarian.getPasswordHash()).authorities("ROLE_LIBRARIAN").build();
    }

}
