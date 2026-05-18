package com.ga.melodi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	@Column(name = "payment_reference", nullable = false, unique = true, length = 60)
	private String paymentReference;

	@Column(name = "payment_method", nullable = false, length = 30)
	private String paymentMethod;

	@Column(name = "payment_status", nullable = false, length = 30)
	private String paymentStatus;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "paid_at")
	private Instant paidAt;
}
