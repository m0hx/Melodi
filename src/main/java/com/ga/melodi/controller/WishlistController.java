package com.ga.melodi.controller;

import com.ga.melodi.model.WishlistItem;
import com.ga.melodi.model.request.AddWishlistItemRequest;
import com.ga.melodi.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

	private final WishlistService wishlistService;

	@GetMapping
	public List<WishlistItem> getWishlist() {
		return wishlistService.getWishlist();
	}

	@PostMapping("/items")
	public WishlistItem addItem(@RequestBody AddWishlistItemRequest request) {
		return wishlistService.addItem(request);
	}

	@DeleteMapping("/items/{wishlistItemId}")
	public void removeItem(@PathVariable Long wishlistItemId) {
		wishlistService.removeItem(wishlistItemId);
	}
}
