package com.ga.melodi.repository;

import com.ga.melodi.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

	boolean existsByOrderNumber(String orderNumber);

	List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

	Optional<Order> findByOrderNumber(String orderNumber);
}
