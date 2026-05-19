package com.ga.melodi.repository;

import com.ga.melodi.model.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

	List<WishlistItem> findByUser_IdOrderByIdAsc(Long userId);

	Optional<WishlistItem> findByIdAndUser_Id(Long id, Long userId);

	boolean existsByUser_IdAndInstrument_Id(Long userId, Long instrumentId);
}
