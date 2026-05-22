package com.ga.melodi.repository;

import com.ga.melodi.model.OrderItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrder_Id(Long orderId);

	long countByInstrument_Id(Long instrumentId);

	@Query(
			"""
			select oi from OrderItem oi
			join oi.order o
			where oi.id = :orderItemId
			and o.user.id = :userId
			and o.status = 'CONFIRMED'
			and oi.instrument.id = :instrumentId
			""")
	Optional<OrderItem> findConfirmedLineForUser(
			@Param("orderItemId") Long orderItemId,
			@Param("userId") Long userId,
			@Param("instrumentId") Long instrumentId);
}
