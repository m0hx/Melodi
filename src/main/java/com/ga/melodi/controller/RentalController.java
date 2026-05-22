package com.ga.melodi.controller;

import com.ga.melodi.model.Rental;
import com.ga.melodi.service.RentalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

	private final RentalService rentalService;

	@GetMapping
	public List<Rental> listMyRentals() {
		return rentalService.listMyRentals();
	}

	@PostMapping("/{rentalId}/return")
	public Rental returnRental(@PathVariable Long rentalId) {
		return rentalService.returnRental(rentalId);
	}
}
