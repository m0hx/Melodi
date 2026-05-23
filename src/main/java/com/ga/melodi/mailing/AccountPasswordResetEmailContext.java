package com.ga.melodi.mailing;

import com.ga.melodi.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountPasswordResetEmailContext extends AbstractEmailContext {

	public void init(User user) {
		put("fullName", user.getFullName());
		setTemplateLocation("mailing/password-reset");
		setSubject("Reset your Melodi password");
		setTo(user.getEmail());
	}

	public void setToken(String token) {
		put("token", token);
	}

	public void buildResetUrl(String baseUrl, String token) {
		String url = UriComponentsBuilder.fromUriString(baseUrl)
				.path("/reset-password")
				.queryParam("token", token)
				.toUriString();
		put("resetURL", url);
	}
}
