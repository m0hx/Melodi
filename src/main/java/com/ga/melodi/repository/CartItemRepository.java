package com.ga.melodi.repository;

import com.ga.melodi.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByUser_IdOrderByIdAsc(Long userId);

	Optional<CartItem> findByIdAndUser_Id(Long id, Long userId);

	Optional<CartItem> findByUser_IdAndInstrument_IdAndMode(Long userId, Long instrumentId, String mode);

	void deleteByUser_Id(Long userId);
}
