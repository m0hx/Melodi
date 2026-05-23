package com.ga.melodi.service;

import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.OrderItem;
import com.ga.melodi.model.Review;
import com.ga.melodi.model.User;
import com.ga.melodi.model.request.CreateReviewRequest;
import com.ga.melodi.model.request.UpdateReviewRequest;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.OrderItemRepository;
import com.ga.melodi.repository.ReviewRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final OrderItemRepository orderItemRepository;
	private final InstrumentRepository instrumentRepository;
	private final CurrentUserService currentUserService;

	public List<Review> listApprovedForInstrument(Long instrumentId) {
		ensureInstrumentExists(instrumentId);
		return reviewRepository.findByInstrument_IdAndApprovedTrueOrderByCreatedAtDesc(instrumentId);
	}

	@Transactional
	public Review createReview(Long instrumentId, CreateReviewRequest request) {
		if (request == null || request.getOrderItemId() == null || request.getRating() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderItemId and rating are required");
		}
		validateRating(request.getRating());
		ensureInstrumentExists(instrumentId);

		User user = currentUserService.getCurrentUser();
		if (reviewRepository.existsByUser_IdAndOrderItem_Id(user.getId(), request.getOrderItemId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this order line");
		}

		OrderItem orderItem = orderItemRepository
				.findConfirmedLineForUser(request.getOrderItemId(), user.getId(), instrumentId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Order line not found or order is not confirmed for this instrument"));

		Review review = new Review();
		review.setUser(user);
		review.setInstrument(orderItem.getInstrument());
		review.setOrderItem(orderItem);
		review.setRating(request.getRating());
		review.setTitle(trimToNull(request.getTitle()));
		review.setBody(trimToNull(request.getBody()));
		review.setReviewerName(user.getFullName());
		review.setApproved(true);
		review.setCreatedAt(Instant.now());
		return reviewRepository.save(review);
	}

	@Transactional
	public Review updateReview(Long reviewId, UpdateReviewRequest request) {
		User user = currentUserService.getCurrentUser();
		Review review = reviewRepository
				.findByIdAndUser_Id(reviewId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

		if (request != null) {
			if (request.getRating() != null) {
				validateRating(request.getRating());
				review.setRating(request.getRating());
			}
			if (request.getTitle() != null) {
				review.setTitle(trimToNull(request.getTitle()));
			}
			if (request.getBody() != null) {
				review.setBody(trimToNull(request.getBody()));
			}
		}
		return reviewRepository.save(review);
	}

	@Transactional
	public void deleteReview(Long reviewId) {
		User user = currentUserService.getCurrentUser();
		Review review = reviewRepository
				.findByIdAndUser_Id(reviewId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
		reviewRepository.delete(review);
	}

	private void ensureInstrumentExists(Long instrumentId) {
		Instrument instrument = instrumentRepository
				.findById(instrumentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found"));
		if ("HIDDEN".equalsIgnoreCase(instrument.getStatus()) && !currentUserService.isCurrentUserAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found");
		}
	}

	private void validateRating(int rating) {
		if (rating < 1 || rating > 5) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
