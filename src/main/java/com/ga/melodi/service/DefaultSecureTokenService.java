package com.ga.melodi.service;

import com.ga.melodi.model.SecureToken;
import com.ga.melodi.repository.SecureTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultSecureTokenService implements SecureTokenService {

	private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(12);

	@Value("${melodi.token-validity-seconds:2800}")
	private int tokenValidityInSeconds;

	private final SecureTokenRepository secureTokenRepository;

	@Override
	public SecureToken createToken() {
		String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(DEFAULT_TOKEN_GENERATOR.generateKey());
		SecureToken secureToken = new SecureToken();
		secureToken.setToken(tokenValue);
		secureToken.setExpireAt(Instant.now().plus(tokenValidityInSeconds, ChronoUnit.SECONDS));
		return secureToken;
	}

	@Override
	public void saveSecureToken(SecureToken secureToken) {
		secureTokenRepository.save(secureToken);
	}

	@Override
	public SecureToken findByToken(String token) {
		return secureTokenRepository.findByToken(token);
	}

	@Override
	public void removeToken(SecureToken token) {
		secureTokenRepository.deleteByToken(token.getToken());
	}
}
