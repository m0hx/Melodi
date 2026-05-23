package com.ga.melodi.service;

import com.ga.melodi.exception.InformationExistException;
import com.ga.melodi.mailing.AbstractEmailContext;
import com.ga.melodi.mailing.AccountPasswordResetEmailContext;
import com.ga.melodi.mailing.AccountVerificationEmailContext;
import com.ga.melodi.mailing.EmailService;
import com.ga.melodi.model.Role;
import com.ga.melodi.model.SecureToken;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.ChangePasswordRequest;
import com.ga.melodi.model.request.LoginRequest;
import com.ga.melodi.model.request.RegisterRequest;
import com.ga.melodi.model.request.ResetPasswordRequest;
import com.ga.melodi.model.response.LoginResponse;
import com.ga.melodi.repository.RoleRepository;
import com.ga.melodi.repository.UserRepository;
import com.ga.melodi.security.JWTUtils;
import com.ga.melodi.security.MyUserDetails;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final int MIN_PASSWORD_LENGTH = 6;

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final SecureTokenService secureTokenService;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final JWTUtils jwtUtils;
	private final CurrentUserService currentUserService;
	@Lazy
	private final AuthenticationManager authenticationManager;

	@Value("${melodi.app.base-url:http://localhost:8080}")
	private String appBaseUrl;

	@Value("${melodi.app.frontend-url:http://localhost:5173}")
	private String frontendBaseUrl;

	@Transactional
	public User createUser(RegisterRequest req) {
		if (req.getEmail() == null || req.getPassword() == null || req.getName() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email, password, and name are required");
		}
		validatePassword(req.getPassword());
		if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
			throw new InformationExistException("User already exist");
		}
		Role userRole = roleRepository
				.findByName("USER")
				.orElseThrow(() -> new IllegalStateException("USER role not seeded — restart the app"));
		User user = new User();
		user.setEmail(req.getEmail().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
		user.setFullName(req.getName().trim());
		user.setRole(userRole);
		user.setEmailVerifiedAt(null);
		user = userRepository.save(user);
		sendConfirmationEmail(user);
		return user;
	}

	public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
		if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
			return ResponseEntity.ok(new LoginResponse("Error : Email or Password is incorrect"));
		}
		var userOpt = userRepository.findByEmailIgnoreCase(loginRequest.getEmail().trim());
		if (userOpt.isPresent() && userOpt.get().getEmailVerifiedAt() == null) {
			return ResponseEntity.ok(new LoginResponse("Error : Email not verified"));
		}
		if (userOpt.isPresent() && !"ACTIVE".equalsIgnoreCase(userOpt.get().getUserStatus())) {
			return ResponseEntity.ok(new LoginResponse("Error : Account is inactive"));
		}
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			MyUserDetails details = (MyUserDetails) authentication.getPrincipal();
			String jwt = jwtUtils.generateJwtToken(details);
			return ResponseEntity.ok(new LoginResponse(jwt));
		} catch (Exception e) {
			return ResponseEntity.ok(new LoginResponse("Error : Email or Password is incorrect"));
		}
	}

	public void sendConfirmationEmail(User user) {
		SecureToken secureToken = secureTokenService.createToken();
		secureToken.setUser(user);
		secureTokenService.saveSecureToken(secureToken);

		AccountVerificationEmailContext context = new AccountVerificationEmailContext();
		context.init(user);
		context.setToken(secureToken.getToken());
		context.buildVerificationUrl(frontendBaseUrl, secureToken.getToken());

		sendMailSafe(context, "verification", context.getContext().get("verificationURL"));
	}

	@Transactional
	public LoginResponse validateEmail(String token) {
		SecureToken secureToken = requireValidToken(token);
		User user = secureToken.getUser();
		user.setEmailVerifiedAt(Instant.now());
		userRepository.save(user);
		secureTokenService.removeToken(secureToken);
		return new LoginResponse("Email verified successfully. You can log in now.");
	}

	@Transactional
	public LoginResponse requestPasswordReset(ResetPasswordRequest request) {
		if (request == null || request.getEmailAddress() == null || request.getEmailAddress().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emailAddress is required");
		}
		String email = request.getEmailAddress().trim().toLowerCase();
		userRepository.findByEmailIgnoreCase(email).ifPresent(this::sendPasswordResetEmail);
		return new LoginResponse("If that email exists, a reset link has been sent.");
	}

	@Transactional
	public LoginResponse resetPasswordActivator(String token, String newPassword) {
		if (newPassword == null || newPassword.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
		}
		validatePassword(newPassword);

		SecureToken secureToken = requireValidToken(token);
		User user = secureToken.getUser();
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		secureTokenService.removeToken(secureToken);
		return new LoginResponse("Password reset successfully. You can log in now.");
	}

	public LoginResponse changePassword(ChangePasswordRequest request) {
		String currentPassword = resolveCurrentPassword(request);
		String newPassword = resolveNewPassword(request);
		if (request == null || currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentPassword and newPassword are required");
		}
		validatePassword(newPassword);
		User user = currentUserService.getCurrentUser();
		if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
		}
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		return new LoginResponse("Password changed successfully");
	}

	private void sendPasswordResetEmail(User user) {
		SecureToken secureToken = secureTokenService.createToken();
		secureToken.setUser(user);
		secureTokenService.saveSecureToken(secureToken);

		AccountPasswordResetEmailContext context = new AccountPasswordResetEmailContext();
		context.init(user);
		context.setToken(secureToken.getToken());
		context.buildResetUrl(frontendBaseUrl, secureToken.getToken());

		sendMailSafe(context, "password reset", context.getContext().get("resetURL"));
	}

	private SecureToken requireValidToken(String token) {
		if (token == null || token.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is required");
		}
		SecureToken secureToken = secureTokenService.findByToken(token.trim());
		if (secureToken == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
		}
		if (secureToken.getExpireAt() == null || secureToken.getExpireAt().isBefore(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
		}
		return secureToken;
	}

	private void sendMailSafe(AbstractEmailContext context, String kind, Object fallbackUrl) {
		System.out.println("✓ " + kind + " link (Postman Verify Email or browser):");
		System.out.println("  " + fallbackUrl);
		try {
			emailService.sendMail(context);
			System.out.println("  Email sent to " + context.getTo());
		} catch (Exception e) {
			System.out.println("  Email failed — use link above. Reason: " + e.getMessage());
		}
	}

	private String resolveCurrentPassword(ChangePasswordRequest request) {
		if (request == null) {
			return null;
		}
		if (request.getCurrentPassword() != null && !request.getCurrentPassword().isBlank()) {
			return request.getCurrentPassword();
		}
		return request.getOldPass();
	}

	private String resolveNewPassword(ChangePasswordRequest request) {
		if (request == null) {
			return null;
		}
		if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
			return request.getNewPassword();
		}
		return request.getNewPass();
	}

	private void validatePassword(String password) {
		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
		}
	}
}
