package com.ga.melodi.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
	private String fullName;
	private String phone;
	private String address;
}
