package com.ga.melodi.controller;

import com.ga.melodi.model.Order;
import com.ga.melodi.model.Rental;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.AdminUpdateUserRequest;
import com.ga.melodi.service.AdminUserService;
import com.ga.melodi.service.OrderService;
import com.ga.melodi.service.OrderService.OrderDetailResponse;
import com.ga.melodi.service.RentalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final OrderService orderService;
	private final RentalService rentalService;
	private final AdminUserService adminUserService;

	@GetMapping("/orders")
	@PreAuthorize("hasAuthority('ADMIN')")
	public List<Order> listAllOrders() {
		return orderService.listAllOrders();
	}

	@GetMapping("/orders/{orderId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public OrderDetailResponse getOrder(@PathVariable Long orderId) {
		return orderService.getOrderForAdmin(orderId);
	}

	@GetMapping("/rentals")
	@PreAuthorize("hasAuthority('ADMIN')")
	public List<Rental> listAllRentals() {
		return rentalService.listAllRentals();
	}

	@PostMapping("/rentals/{rentalId}/return")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Rental returnRental(@PathVariable Long rentalId) {
		return rentalService.returnRentalAsAdmin(rentalId);
	}

	@GetMapping("/users")
	@PreAuthorize("hasAuthority('ADMIN')")
	public List<User> listUsers() {
		return adminUserService.listUsers();
	}

	@GetMapping("/users/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public User getUser(@PathVariable Long userId) {
		return adminUserService.getUser(userId);
	}

	@PatchMapping("/users/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public User updateUser(@PathVariable Long userId, @RequestBody AdminUpdateUserRequest request) {
		return adminUserService.updateUser(userId, request);
	}
}
