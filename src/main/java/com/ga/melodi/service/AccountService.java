package com.ga.melodi.service;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.UpdateProfileRequest;
import com.ga.melodi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;

	public User getProfile() {
		return currentUserService.getCurrentUser();
	}

	public User updateProfile(UpdateProfileRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		User user = currentUserService.getCurrentUser();
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
}
