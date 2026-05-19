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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "cart_items",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "instrument_id", "mode"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_id", nullable = false)
	private Instrument instrument;

	@Column(nullable = false, length = 20)
	private String mode;

	@Column(nullable = false)
	private Integer quantity = 1;

	@Column(name = "rental_start_date")
	private Instant rentalStartDate;

	@Column(name = "rental_end_date")
	private Instant rentalEndDate;
}
