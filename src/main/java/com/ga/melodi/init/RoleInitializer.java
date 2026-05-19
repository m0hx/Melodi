package com.ga.melodi.init;

import com.ga.melodi.model.Role;
import com.ga.melodi.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

	private final RoleRepository roleRepository;

	@PostConstruct
	public void init() {
		if (roleRepository.findByName("ADMIN").isEmpty()) {
			roleRepository.save(new Role(null, "ADMIN"));
		}
		if (roleRepository.findByName("USER").isEmpty()) {
			roleRepository.save(new Role(null, "USER"));
		}
	}
}
