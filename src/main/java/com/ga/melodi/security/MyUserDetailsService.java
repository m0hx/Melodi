package com.ga.melodi.security;

import com.ga.melodi.model.User;
import com.ga.melodi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository
				.findByEmailIgnoreCase(email.trim())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
		return new MyUserDetails(user);
	}
}
