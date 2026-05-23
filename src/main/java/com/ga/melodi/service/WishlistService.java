package com.ga.melodi.service;

import com.ga.melodi.exception.InformationExistException;
import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.User;
import com.ga.melodi.model.WishlistItem;
import com.ga.melodi.model.request.AddWishlistItemRequest;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.WishlistItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WishlistService {

	private final WishlistItemRepository wishlistItemRepository;
	private final InstrumentRepository instrumentRepository;
	private final CurrentUserService currentUserService;

	public List<WishlistItem> getWishlist() {
		User user = currentUserService.getCurrentUser();
		List<WishlistItem> items = wishlistItemRepository.findByUser_IdOrderByIdAsc(user.getId());
		if (currentUserService.isCurrentUserAdmin()) {
			return items;
		}
		return items.stream()
				.filter(item -> item.getInstrument() != null
						&& !"HIDDEN".equalsIgnoreCase(item.getInstrument().getStatus()))
				.toList();
	}

	public WishlistItem addItem(AddWishlistItemRequest request) {
		if (request == null || request.getInstrumentId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "instrumentId is required");
		}
		User user = currentUserService.getCurrentUser();
		if (wishlistItemRepository.existsByUser_IdAndInstrument_Id(user.getId(), request.getInstrumentId())) {
			throw new InformationExistException("Instrument is already in your wishlist");
		}
		Instrument instrument = instrumentRepository
				.findById(request.getInstrumentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found"));
		if ("HIDDEN".equalsIgnoreCase(instrument.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument cannot be added to wishlist");
		}
		WishlistItem item = new WishlistItem();
		item.setUser(user);
		item.setInstrument(instrument);
		return wishlistItemRepository.save(item);
	}

	public void removeItem(Long wishlistItemId) {
		User user = currentUserService.getCurrentUser();
		WishlistItem item = wishlistItemRepository
				.findByIdAndUser_Id(wishlistItemId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found"));
		wishlistItemRepository.delete(item);
	}
}
