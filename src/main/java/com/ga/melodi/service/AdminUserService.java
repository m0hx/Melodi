package com.ga.melodi.service;

import com.ga.melodi.model.Role;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.AdminUpdateUserRequest;
import com.ga.melodi.repository.RoleRepository;
import com.ga.melodi.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private static final String ACTIVE = "ACTIVE";
	private static final String INACTIVE = "INACTIVE";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final CurrentUserService currentUserService;

	public List<User> listUsers() {
		return userRepository.findAllByOrderByFullNameAsc();
	}

	public User getUser(Long userId) {
		return userRepository
				.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
	}

	@Transactional
	public User updateUser(Long userId, AdminUpdateUserRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		User user = getUser(userId);
		User admin = currentUserService.getCurrentUser();

		if (request.getFullName() != null && !request.getFullName().isBlank()) {
			user.setFullName(request.getFullName().trim());
		}
		if (request.getPhone() != null) {
			user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
		}
		if (request.getAddress() != null) {
			user.setAddress(request.getAddress().isBlank() ? null : request.getAddress().trim());
		}

		if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
			String roleName = request.getRoleName().trim().toUpperCase();
			if (!"USER".equals(roleName) && !"ADMIN".equals(roleName)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleName must be USER or ADMIN");
			}
			if (user.getId().equals(admin.getId()) && !"ADMIN".equals(roleName)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own admin role");
			}
			if ("ADMIN".equals(user.getRole().getName()) && !"ADMIN".equals(roleName)) {
				ensureNotLastActiveAdmin(user);
			}
			Role role = roleRepository
					.findByName(roleName)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName));
			user.setRole(role);
		}

		if (request.getUserStatus() != null && !request.getUserStatus().isBlank()) {
			String status = request.getUserStatus().trim().toUpperCase();
			if (!ACTIVE.equals(status) && !INACTIVE.equals(status)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userStatus must be ACTIVE or INACTIVE");
			}
			if (user.getId().equals(admin.getId()) && INACTIVE.equals(status)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
			}
			if ("ADMIN".equals(user.getRole().getName()) && INACTIVE.equals(status)) {
				ensureNotLastActiveAdmin(user);
			}
			user.setUserStatus(status);
		}

		return userRepository.save(user);
	}

	private void ensureNotLastActiveAdmin(User user) {
		if (!"ADMIN".equals(user.getRole().getName()) || !ACTIVE.equalsIgnoreCase(user.getUserStatus())) {
			return;
		}
		if (userRepository.countByRole_NameIgnoreCaseAndUserStatusIgnoreCase("ADMIN", ACTIVE) <= 1) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Cannot deactivate or demote the last active admin");
		}
	}
}
