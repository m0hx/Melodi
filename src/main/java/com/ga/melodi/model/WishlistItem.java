package com.ga.melodi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "wishlist_items",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "instrument_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_id", nullable = false)
	private Instrument instrument;
}
