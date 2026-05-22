package com.ga.melodi.controller;

import com.ga.melodi.model.User;
import com.ga.melodi.model.request.ActivatePasswordRequest;
import com.ga.melodi.model.request.ChangePasswordRequest;
import com.ga.melodi.model.request.LoginRequest;
import com.ga.melodi.model.request.RegisterRequest;
import com.ga.melodi.model.request.ResetPasswordRequest;
import com.ga.melodi.model.response.LoginResponse;
import com.ga.melodi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public User register(@RequestBody RegisterRequest request) {
		return userService.createUser(request);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		return userService.loginUser(loginRequest);
	}

	@GetMapping("/register/verify")
	public LoginResponse verifyEmail(@RequestParam String token) {
		return userService.validateEmail(token);
	}

	@PostMapping("/resetPassword")
	public LoginResponse requestPasswordReset(@RequestBody ResetPasswordRequest request) {
		return userService.requestPasswordReset(request);
	}

	@PostMapping(value = "/resetPassword", params = "token")
	public LoginResponse resetPasswordActivator(
			@RequestParam String token, @RequestBody ActivatePasswordRequest request) {
		return userService.resetPasswordActivator(token, request != null ? request.getPassword() : null);
	}

	@PutMapping("/change-password")
	public LoginResponse changePassword(@RequestBody ChangePasswordRequest request) {
		return userService.changePassword(request);
	}
}
