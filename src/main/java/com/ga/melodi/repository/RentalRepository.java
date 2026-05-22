package com.ga.melodi.repository;

import com.ga.melodi.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

	List<Rental> findByUser_IdOrderByStartsAtDesc(Long userId);

	Optional<Rental> findByIdAndUser_Id(Long rentalId, Long userId);

	boolean existsByOrderItem_Id(Long orderItemId);
}
