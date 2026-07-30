package com.uniproject.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContext;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final LibrarianUserDetailsService librarianUserDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, LibrarianUserDetailsService librarianUserDetailsService) {
        this.jwtService = jwtService;
        this.librarianUserDetailsService = librarianUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.substring(7);

            try{
                String username = jwtService.extractUsername(token);

                boolean notAlreadyAuthenticated = SecurityContextHolder.getContext().getAuthentication() == null;

                if (username != null && notAlreadyAuthenticated) {
                    UserDetails userDetails = librarianUserDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                        securityContext.setAuthentication(authentication);
                        SecurityContextHolder.setContext(securityContext);
                    }
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
                //Invalid token
            }

            filterChain.doFilter(request, response);
        }
}
