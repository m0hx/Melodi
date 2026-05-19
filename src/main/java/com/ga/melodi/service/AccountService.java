package com.ga.melodi.service;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.UpdateProfileRequest;
import com.ga.melodi.repository.UserRepository;
import com.ga.melodi.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository userRepository;

	public User getProfile() {
		return findCurrentUser();
	}

	public User updateProfile(UpdateProfileRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		User user = findCurrentUser();
		if (request.getFullName() != null) {
			String fullName = request.getFullName().trim();
			if (fullName.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name cannot be empty");
			}
			user.setFullName(fullName);
		}
		if (request.getPhone() != null) {
			user.setPhone(request.getPhone().trim());
		}
		if (request.getAddress() != null) {
			user.setAddress(request.getAddress().trim());
		}
		return userRepository.save(user);
	}

	private User findCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails details)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		return userRepository
				.findByEmailIgnoreCase(details.getUsername())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}
}
