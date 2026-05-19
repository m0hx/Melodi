package com.ga.melodi.controller;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.UpdateProfileRequest;
import com.ga.melodi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
