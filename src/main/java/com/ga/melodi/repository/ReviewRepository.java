package com.ga.melodi.repository;

import com.ga.melodi.model.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByInstrument_IdAndApprovedTrueOrderByCreatedAtDesc(Long instrumentId);

	Optional<Review> findByIdAndUser_Id(Long reviewId, Long userId);

	boolean existsByUser_IdAndOrderItem_Id(Long userId, Long orderItemId);
}
