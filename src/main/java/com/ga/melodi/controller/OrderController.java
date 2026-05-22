package com.ga.melodi.controller;

import com.ga.melodi.model.Order;
import com.ga.melodi.model.request.ConfirmPaymentRequest;
import com.ga.melodi.service.OrderService;
import com.ga.melodi.service.OrderService.OrderDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/checkout")
	public OrderDetailResponse checkout() {
		return orderService.checkout();
	}

	@PostMapping("/{orderId}/confirm-payment")
	public OrderDetailResponse confirmPayment(
			@PathVariable Long orderId, @RequestBody(required = false) ConfirmPaymentRequest request) {
		return orderService.confirmPayment(orderId, request);
	}

	@GetMapping
	public List<Order> listMyOrders() {
		return orderService.listMyOrders();
	}

	@GetMapping("/{orderId}")
	public OrderDetailResponse getMyOrder(@PathVariable Long orderId) {
		return orderService.getMyOrder(orderId);
	}

	@PostMapping("/{orderId}/cancel")
	public Order cancelOrder(@PathVariable Long orderId) {
		return orderService.cancelOrder(orderId);
	}
}
