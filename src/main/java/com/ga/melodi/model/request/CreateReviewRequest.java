package com.ga.melodi.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

	private Long orderItemId;
	private Integer rating;
	private String title;
	private String body;
}
