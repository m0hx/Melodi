package com.ga.melodi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "Melodi Instruments API is up. React frontend will be a separate app.";
	}

	@GetMapping("/login")
	public String loginInfo() {
		return "Login UI: React app. API: POST /auth/users/login";
	}
}
