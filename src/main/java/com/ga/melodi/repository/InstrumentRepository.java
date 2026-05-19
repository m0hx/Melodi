package com.ga.melodi.repository;

import com.ga.melodi.model.Instrument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

	Optional<Instrument> findByNameIgnoreCase(String name);

	List<Instrument> findByStatus(String status);

	List<Instrument> findByCategory_Id(Long categoryId);

	List<Instrument> findByBrand_Id(Long brandId);

	long countByCategory_Id(Long categoryId);

	long countByBrand_Id(Long brandId);
}
