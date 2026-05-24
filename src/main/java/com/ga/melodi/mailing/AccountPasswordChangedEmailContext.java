package com.ga.melodi.mailing;

import com.ga.melodi.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountPasswordChangedEmailContext extends AbstractEmailContext {

	public void init(User user) {
		put("fullName", user.getFullName());
		setTemplateLocation("mailing/password-changed");
		setSubject("Your Melodi password was changed");
		setTo(user.getEmail());
	}

	public void buildSignInUrl(String baseUrl) {
		put("signInURL", UriComponentsBuilder.fromUriString(baseUrl).path("/signin").toUriString());
	}
}
