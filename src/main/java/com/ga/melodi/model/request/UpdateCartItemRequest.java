package com.ga.melodi.model.request;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {
	private Integer quantity;
	private Instant rentalStartDate;
	private Instant rentalEndDate;
}
