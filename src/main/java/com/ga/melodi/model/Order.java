package com.ga.melodi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "order_number", nullable = false, unique = true, length = 40)
	private String orderNumber;

	@Column(name = "order_type", nullable = false, length = 30)
	private String orderType = "PURCHASE";

	@Column(nullable = false, length = 30)
	private String status = "PENDING_PAYMENT";

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "tracking_id", unique = true, length = 60)
	private String trackingId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
