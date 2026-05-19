package com.ga.melodi.repository;

import com.ga.melodi.model.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrder_Id(Long orderId);

	long countByInstrument_Id(Long instrumentId);
}
