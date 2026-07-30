package com.uniproject.library.security;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;



@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder().subject(username).issuedAt(Date.from(now)).expiration(Date.from(expiration)).signWith(signingKey).compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims  claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();

            return expectedUsername != null && expectedUsername.equals(claims.getSubject()) && expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
