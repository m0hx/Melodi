package com.ga.melodi.service;

import com.ga.melodi.model.CartItem;
import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.Order;
import com.ga.melodi.model.OrderItem;
import com.ga.melodi.model.Payment;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.ConfirmPaymentRequest;
import com.ga.melodi.repository.CartItemRepository;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.OrderItemRepository;
import com.ga.melodi.repository.OrderRepository;
import com.ga.melodi.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final PaymentRepository paymentRepository;
	private final CartItemRepository cartItemRepository;
	private final InstrumentRepository instrumentRepository;
	private final CurrentUserService currentUserService;

	public record OrderDetailResponse(Order order, List<OrderItem> items, Payment payment) {}

	@Transactional
	public OrderDetailResponse checkout() {
		User user = currentUserService.getCurrentUser();
		List<CartItem> cartItems = cartItemRepository.findByUser_IdOrderByIdAsc(user.getId());
		if (cartItems.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
		}

		List<OrderItem> lineItems = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;
		boolean hasBuy = false;
		boolean hasRent = false;

		for (CartItem cartItem : cartItems) {
			Instrument instrument = instrumentRepository
					.findById(cartItem.getInstrument().getId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument not found"));
			validateInstrumentForOrder(instrument);
			String mode = cartItem.getMode();
			int quantity = cartItem.getQuantity();
			validateLineStock(instrument, mode, quantity, cartItem.getRentalStartDate(), cartItem.getRentalEndDate());

			BigDecimal unitPrice;
			Integer rentalDays = null;
			Instant rentalStart = null;
			Instant rentalEnd = null;

			if ("BUY".equals(mode)) {
				hasBuy = true;
				unitPrice = instrument.getPurchasePrice();
			} else {
				hasRent = true;
				rentalStart = cartItem.getRentalStartDate();
				rentalEnd = cartItem.getRentalEndDate();
				rentalDays = rentalDaysBetween(rentalStart, rentalEnd);
				unitPrice = instrument.getRentalPricePerDay()
						.multiply(BigDecimal.valueOf(rentalDays))
						.setScale(2, RoundingMode.HALF_UP);
			}

			BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
			total = total.add(lineTotal);

			OrderItem line = new OrderItem();
			line.setInstrument(instrument);
			line.setMode(mode);
			line.setQuantity(quantity);
			line.setUnitPrice(unitPrice);
			line.setRentalDays(rentalDays);
			line.setRentalStartDate(rentalStart);
			line.setRentalEndDate(rentalEnd);
			lineItems.add(line);
		}

		Order order = new Order();
		order.setUser(user);
		order.setOrderNumber(generateOrderNumber());
		order.setOrderType(resolveOrderType(hasBuy, hasRent));
		order.setStatus("PENDING_PAYMENT");
		order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
		order.setCreatedAt(Instant.now());
		order = orderRepository.save(order);

		for (OrderItem line : lineItems) {
			line.setOrder(order);
			orderItemRepository.save(line);
		}

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentReference(generatePaymentReference());
		payment.setPaymentMethod("SIMULATED");
		payment.setPaymentStatus("PENDING");
		payment.setAmount(order.getTotalAmount());
		payment = paymentRepository.save(payment);

		return new OrderDetailResponse(order, orderItemRepository.findByOrder_Id(order.getId()), payment);
	}

	@Transactional
	public OrderDetailResponse confirmPayment(Long orderId, ConfirmPaymentRequest request) {
		User user = currentUserService.getCurrentUser();
		Order order = orderRepository
				.findByIdAndUser_Id(orderId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

		if (!"PENDING_PAYMENT".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
		}

		Payment payment = paymentRepository
				.findByOrder_Id(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

		List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
		for (OrderItem item : items) {
			Instrument instrument = instrumentRepository
					.findById(item.getInstrument().getId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument not found"));
			validateInstrumentForOrder(instrument);
			validateLineStock(
					instrument, item.getMode(), item.getQuantity(), item.getRentalStartDate(), item.getRentalEndDate());
			applyStockChange(instrument, item.getMode(), item.getQuantity());
			instrumentRepository.save(instrument);
		}

		if (request != null && request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
			payment.setPaymentMethod(request.getPaymentMethod().trim());
		}
		payment.setPaymentStatus("SIMULATED_PAID");
		payment.setPaidAt(Instant.now());
		paymentRepository.save(payment);

		order.setStatus("CONFIRMED");
		order.setTrackingId(generateTrackingId());
		orderRepository.save(order);

		cartItemRepository.deleteByUser_Id(user.getId());

		return new OrderDetailResponse(order, items, payment);
	}

	public List<Order> listMyOrders() {
		User user = currentUserService.getCurrentUser();
		return orderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
	}

	public OrderDetailResponse getMyOrder(Long orderId) {
		User user = currentUserService.getCurrentUser();
		Order order = orderRepository
				.findByIdAndUser_Id(orderId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		Payment payment = paymentRepository.findByOrder_Id(orderId).orElse(null);
		List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
		return new OrderDetailResponse(order, items, payment);
	}

	@Transactional
	public Order cancelOrder(Long orderId) {
		User user = currentUserService.getCurrentUser();
		Order order = orderRepository
				.findByIdAndUser_Id(orderId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

		if (!"PENDING_PAYMENT".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending orders can be cancelled");
		}

		order.setStatus("CANCELLED");
		paymentRepository.findByOrder_Id(orderId).ifPresent(payment -> {
			payment.setPaymentStatus("FAILED");
			paymentRepository.save(payment);
		});
		return orderRepository.save(order);
	}

	private void validateInstrumentForOrder(Instrument instrument) {
		if (!"AVAILABLE".equalsIgnoreCase(instrument.getStatus())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Instrument is not available: " + instrument.getStatus());
		}
	}

	private void validateLineStock(
			Instrument instrument, String mode, int quantity, Instant rentalStart, Instant rentalEnd) {
		if ("BUY".equals(mode)) {
			if (instrument.getPurchaseStock() < quantity) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Not enough purchase stock for " + instrument.getName() + " (available: "
								+ instrument.getPurchaseStock() + ")");
			}
			return;
		}
		if (instrument.getRentalStock() < quantity) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Not enough rental stock for " + instrument.getName() + " (available: "
							+ instrument.getRentalStock() + ")");
		}
		if (rentalStart == null || rentalEnd == null || !rentalEnd.isAfter(rentalStart)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rental dates on order line");
		}
	}

	private void applyStockChange(Instrument instrument, String mode, int quantity) {
		if ("BUY".equals(mode)) {
			instrument.setPurchaseStock(instrument.getPurchaseStock() - quantity);
		} else {
			instrument.setRentalStock(instrument.getRentalStock() - quantity);
		}
	}

	private int rentalDaysBetween(Instant start, Instant end) {
		long days = ChronoUnit.DAYS.between(start.atZone(ZoneOffset.UTC).toLocalDate(), end.atZone(ZoneOffset.UTC).toLocalDate());
		return (int) Math.max(1, days);
	}

	private String resolveOrderType(boolean hasBuy, boolean hasRent) {
		if (hasBuy && hasRent) {
			return "MIXED";
		}
		if (hasRent) {
			return "RENTAL";
		}
		return "PURCHASE";
	}

	private String generateOrderNumber() {
		for (int i = 0; i < 10; i++) {
			String candidate = "MEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
			if (!orderRepository.existsByOrderNumber(candidate)) {
				return candidate;
			}
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate order number");
	}

	private String generatePaymentReference() {
		return "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
	}

	private String generateTrackingId() {
		return "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
	}
}
