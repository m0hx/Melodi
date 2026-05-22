package com.ga.melodi.service;

import com.ga.melodi.model.SecureToken;

public interface SecureTokenService {

	SecureToken createToken();

	void saveSecureToken(SecureToken secureToken);

	SecureToken findByToken(String token);

	void removeToken(SecureToken token);
}
