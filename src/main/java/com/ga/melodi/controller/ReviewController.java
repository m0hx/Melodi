package com.ga.melodi.controller;

import com.ga.melodi.model.Review;
import com.ga.melodi.model.request.CreateReviewRequest;
import com.ga.melodi.model.request.UpdateReviewRequest;
import com.ga.melodi.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@GetMapping("/api/instruments/{instrumentId}/reviews")
	public List<Review> listInstrumentReviews(@PathVariable Long instrumentId) {
		return reviewService.listApprovedForInstrument(instrumentId);
	}

	@PostMapping("/api/instruments/{instrumentId}/reviews")
	public Review createReview(@PathVariable Long instrumentId, @RequestBody CreateReviewRequest request) {
		return reviewService.createReview(instrumentId, request);
	}

	@PatchMapping("/api/reviews/{reviewId}")
	public Review updateReview(@PathVariable Long reviewId, @RequestBody UpdateReviewRequest request) {
		return reviewService.updateReview(reviewId, request);
	}

	@DeleteMapping("/api/reviews/{reviewId}")
	public void deleteReview(@PathVariable Long reviewId) {
		reviewService.deleteReview(reviewId);
	}
}
