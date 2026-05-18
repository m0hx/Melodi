package com.ga.melodi.service;

import com.ga.melodi.exception.InformationExistException;
import com.ga.melodi.model.Role;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.LoginRequest;
import com.ga.melodi.model.request.RegisterRequest;
import com.ga.melodi.model.response.LoginResponse;
import com.ga.melodi.repository.RoleRepository;
import com.ga.melodi.repository.UserRepository;
import com.ga.melodi.security.JWTUtils;
import com.ga.melodi.security.MyUserDetails;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTUtils jwtUtils;
	@Lazy
	private final AuthenticationManager authenticationManager;

	public User createUser(RegisterRequest req) {
		if (req.getEmail() == null || req.getPassword() == null || req.getName() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email, password, and name are required");
		}
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
		// TODO: set null until email verified; temporarily set so login works before verify flow is implemented
		user.setEmailVerifiedAt(Instant.now());
		return userRepository.save(user);
	}

	public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			MyUserDetails details = (MyUserDetails) authentication.getPrincipal();
			if (!details.isEnabled()) {
				return ResponseEntity.ok(new LoginResponse("Error : Email not verified"));
			}
			String jwt = jwtUtils.generateJwtToken(details);
			return ResponseEntity.ok(new LoginResponse(jwt));
		} catch (Exception e) {
			return ResponseEntity.ok(new LoginResponse("Error : Email or Password is incorrect"));
		}
	}
}
