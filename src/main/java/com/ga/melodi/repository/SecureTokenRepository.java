package com.ga.melodi.repository;

import com.ga.melodi.model.SecureToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecureTokenRepository extends JpaRepository<SecureToken, Long> {

	SecureToken findByToken(String token);

	void deleteByToken(String token);
}
