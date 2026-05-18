package com.ga.melodi.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtUtils {

	private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

	private final SecretKey key;
	private final int jwtExpirationMs;

	public JwtUtils(
			@Value("${jwt-secret}") String rawSecret,
			@Value("${jwt-expiration-ms:86400000}") int jwtExpirationMs) {
		String secret = rawSecret.replace("\"", "").trim();
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.jwtExpirationMs = jwtExpirationMs;
	}

	public String generateJwtToken(MyUserDetails userDetails) {
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(key)
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
			return true;
		} catch (SecurityException e) {
			log.warn("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.warn("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.warn("JWT expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("JWT unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims empty: {}", e.getMessage());
		}
		return false;
	}
}
