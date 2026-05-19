package com.ga.melodi.controller;

import com.ga.melodi.model.CartItem;
import com.ga.melodi.model.request.AddCartItemRequest;
import com.ga.melodi.model.request.UpdateCartItemRequest;
import com.ga.melodi.service.CartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@GetMapping
	public List<CartItem> getCart() {
		return cartService.getCart();
	}

	@PostMapping("/items")
	public CartItem addItem(@RequestBody AddCartItemRequest request) {
		return cartService.addItem(request);
	}

	@PatchMapping("/items/{cartItemId}")
	public CartItem updateItem(@PathVariable Long cartItemId, @RequestBody UpdateCartItemRequest request) {
		return cartService.updateItem(cartItemId, request);
	}

	@DeleteMapping("/items/{cartItemId}")
	public void removeItem(@PathVariable Long cartItemId) {
		cartService.removeItem(cartItemId);
	}

	@DeleteMapping
	public void clearCart() {
		cartService.clearCart();
	}
}
