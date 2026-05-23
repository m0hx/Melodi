package com.ga.melodi.service;

import com.ga.melodi.model.User;
import com.ga.melodi.repository.UserRepository;
import com.ga.melodi.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final UserRepository userRepository;

	public User getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails details)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		return userRepository
				.findByEmailIgnoreCase(details.getUsername())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public boolean isCurrentUserAdmin() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails details)) {
			return false;
		}
		return details.getAuthorities().stream()
				.anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()));
	}
}
