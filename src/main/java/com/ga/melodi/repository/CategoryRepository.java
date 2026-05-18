package com.ga.melodi.repository;

import com.ga.melodi.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsByNameIgnoreCase(String name);

	Optional<Category> findByNameIgnoreCase(String name);
}
