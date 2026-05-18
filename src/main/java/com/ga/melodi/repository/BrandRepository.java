package com.ga.melodi.repository;

import com.ga.melodi.model.Brand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

	boolean existsByNameIgnoreCase(String name);

	Optional<Brand> findByNameIgnoreCase(String name);
}
