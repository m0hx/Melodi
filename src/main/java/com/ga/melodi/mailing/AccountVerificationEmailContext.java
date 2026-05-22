package com.ga.melodi.mailing;

import com.ga.melodi.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountVerificationEmailContext extends AbstractEmailContext {

	public void init(User user) {
		put("fullName", user.getFullName());
		setTemplateLocation("mailing/email-verification");
		setSubject("Complete your Melodi registration");
		setTo(user.getEmail());
	}

	public void setToken(String token) {
		put("token", token);
	}

	public void buildVerificationUrl(String baseUrl, String token) {
		String url = UriComponentsBuilder.fromUriString(baseUrl)
				.path("/auth/users/register/verify")
				.queryParam("token", token)
				.toUriString();
		put("verificationURL", url);
	}
}
