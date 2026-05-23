package com.ga.melodi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(length = 2000)
	private String description;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal purchasePrice;

	@Column(name = "rental_price_per_day", nullable = false, precision = 10, scale = 2)
	private BigDecimal rentalPricePerDay;

	@Column(name = "purchase_stock", nullable = false)
	private Integer purchaseStock = 0;

	@Column(name = "rental_stock", nullable = false)
	private Integer rentalStock = 0;

	@Column(nullable = false, length = 30)
	private String condition = "NEW";

	@Column(nullable = false, length = 30)
	private String status = "AVAILABLE";

	private String imageName;
	private String imageType;

	@JsonIgnore
	@Column(name = "image_data", columnDefinition = "BYTEA")
	private byte[] imageData;
}
