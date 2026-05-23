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
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "full_name", nullable = false, length = 120)
	private String fullName;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@JsonIgnore
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(length = 30)
	private String phone;

	@Column(length = 500)
	private String address;

	@JsonIgnore
	@Column(name = "profile_image_path", length = 500)
	private String profileImagePath;

	private String imageName;
	private String imageType;

	@JsonIgnore
	@Column(name = "image_data", columnDefinition = "BYTEA")
	private byte[] imageData;

	@Column(name = "user_status", nullable = false, length = 20)
	private String userStatus = "ACTIVE";

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;
}
