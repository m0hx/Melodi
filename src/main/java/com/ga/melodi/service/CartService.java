package com.ga.melodi.service;

import com.ga.melodi.model.CartItem;
import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.AddCartItemRequest;
import com.ga.melodi.model.request.UpdateCartItemRequest;
import com.ga.melodi.repository.CartItemRepository;
import com.ga.melodi.repository.InstrumentRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final InstrumentRepository instrumentRepository;
	private final CurrentUserService currentUserService;

	public List<CartItem> getCart() {
		User user = currentUserService.getCurrentUser();
		return cartItemRepository.findByUser_IdOrderByIdAsc(user.getId());
	}

	public CartItem addItem(AddCartItemRequest request) {
		if (request == null || request.getInstrumentId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "instrumentId is required");
		}
		User user = currentUserService.getCurrentUser();
		Instrument instrument = resolveInstrument(request.getInstrumentId());
		validateInstrumentForCart(instrument);

		String mode = normalizeMode(request.getMode());
		int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
		if (quantity < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
		}

		var existing = cartItemRepository.findByUser_IdAndInstrument_IdAndMode(
				user.getId(), instrument.getId(), mode);
		if (existing.isPresent()) {
			CartItem item = existing.get();
			int newQuantity = item.getQuantity() + quantity;
			applyCartRules(instrument, mode, newQuantity, request.getRentalStartDate(), request.getRentalEndDate());
			item.setQuantity(newQuantity);
			if ("RENT".equals(mode)) {
				item.setRentalStartDate(request.getRentalStartDate());
				item.setRentalEndDate(request.getRentalEndDate());
			}
			return cartItemRepository.save(item);
		}

		applyCartRules(instrument, mode, quantity, request.getRentalStartDate(), request.getRentalEndDate());
		CartItem item = new CartItem();
		item.setUser(user);
		item.setInstrument(instrument);
		item.setMode(mode);
		item.setQuantity(quantity);
		if ("RENT".equals(mode)) {
			item.setRentalStartDate(request.getRentalStartDate());
			item.setRentalEndDate(request.getRentalEndDate());
		}
		return cartItemRepository.save(item);
	}

	public CartItem updateItem(Long cartItemId, UpdateCartItemRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		User user = currentUserService.getCurrentUser();
		CartItem item = cartItemRepository
				.findByIdAndUser_Id(cartItemId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

		int quantity = request.getQuantity() != null ? request.getQuantity() : item.getQuantity();
		Instant start = request.getRentalStartDate() != null ? request.getRentalStartDate() : item.getRentalStartDate();
		Instant end = request.getRentalEndDate() != null ? request.getRentalEndDate() : item.getRentalEndDate();

		applyCartRules(item.getInstrument(), item.getMode(), quantity, start, end);
		item.setQuantity(quantity);
		if ("RENT".equals(item.getMode())) {
			item.setRentalStartDate(start);
			item.setRentalEndDate(end);
		}
		return cartItemRepository.save(item);
	}

	public void removeItem(Long cartItemId) {
		User user = currentUserService.getCurrentUser();
		CartItem item = cartItemRepository
				.findByIdAndUser_Id(cartItemId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
		cartItemRepository.delete(item);
	}

	@Transactional
	public void clearCart() {
		User user = currentUserService.getCurrentUser();
		cartItemRepository.deleteByUser_Id(user.getId());
	}

	private Instrument resolveInstrument(Long instrumentId) {
		return instrumentRepository
				.findById(instrumentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found"));
	}

	private void validateInstrumentForCart(Instrument instrument) {
		if (!"AVAILABLE".equalsIgnoreCase(instrument.getStatus())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Instrument is not available for cart: " + instrument.getStatus());
		}
	}

	private String normalizeMode(String mode) {
		if (mode == null || mode.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode is required (BUY or RENT)");
		}
		String normalized = mode.trim().toUpperCase();
		if (!"BUY".equals(normalized) && !"RENT".equals(normalized)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be BUY or RENT");
		}
		return normalized;
	}

	private void applyCartRules(Instrument instrument, String mode, int quantity, Instant rentalStart, Instant rentalEnd) {
		if ("BUY".equals(mode)) {
			if (instrument.getPurchaseStock() < quantity) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Not enough purchase stock (available: " + instrument.getPurchaseStock() + ")");
			}
			return;
		}
		if (instrument.getRentalStock() <= 0
				|| instrument.getRentalPricePerDay().signum() == 0
				|| "Accessories".equals(instrument.getCategory().getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument is not available for rent");
		}
		if (rentalStart == null || rentalEnd == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental start and end dates are required");
		}
		if (!rentalEnd.isAfter(rentalStart)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental end date must be after start date");
		}
		if (instrument.getRentalStock() < quantity) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Not enough rental stock (available: " + instrument.getRentalStock() + ")");
		}
	}
}
