package com.ga.melodi.service;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.UpdateProfileRequest;
import com.ga.melodi.repository.UserRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

	public User updateProfileImage(Long userId, MultipartFile image) throws IOException {
		User user = resolveUserForImageUpdate(userId);
		validateImage(image);
		user.setImageName(image.getOriginalFilename());
		user.setImageType(image.getContentType());
		user.setImageData(image.getBytes());
		return userRepository.save(user);
	}

	public User getProfileForImage(Long userId) {
		return userRepository
				.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User removeProfileImage(Long userId) {
		User user = resolveUserForImageUpdate(userId);
		user.setImageName(null);
		user.setImageType(null);
		user.setImageData(null);
		return userRepository.save(user);
	}

	private User resolveUserForImageUpdate(Long userId) {
		User current = currentUserService.getCurrentUser();
		if (!current.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile image");
		}
		return current;
	}

	private static void validateImage(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
		}
		String contentType = image.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image");
		}
	}
}
