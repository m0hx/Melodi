package com.ga.melodi.controller;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.UpdateProfileRequest;
import com.ga.melodi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@GetMapping
	public User getProfile() {
		return accountService.getProfile();
	}

	@PatchMapping
	public User updateProfile(@RequestBody UpdateProfileRequest request) {
		return accountService.updateProfile(request);
	}

	@PutMapping("/{userId}/image")
	public ResponseEntity<String> updateProfileImage(
			@PathVariable Long userId, @RequestParam("image") MultipartFile image) throws Exception {
		accountService.updateProfileImage(userId, image);
		return ResponseEntity.ok("Profile image updated successfully");
	}

	@GetMapping("/{userId}/image")
	public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
		User user = accountService.getProfileForImage(userId);
		if (user.getImageData() == null || user.getImageData().length == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
				.header("Content-Type", user.getImageType() != null ? user.getImageType() : "image/jpeg")
				.body(user.getImageData());
	}

	@DeleteMapping("/{userId}/image")
	public ResponseEntity<String> removeProfileImage(@PathVariable Long userId) {
		accountService.removeProfileImage(userId);
		return ResponseEntity.ok("Profile image removed successfully");
	}
}
