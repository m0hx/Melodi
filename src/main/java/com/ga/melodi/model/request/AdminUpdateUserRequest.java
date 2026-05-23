package com.ga.melodi.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRequest {
	private String fullName;
	private String phone;
	private String address;
	private String roleName;
	private String userStatus;
}
