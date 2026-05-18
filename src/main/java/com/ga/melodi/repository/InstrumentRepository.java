package com.ga.melodi.repository;

import com.ga.melodi.model.Instrument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

	List<Instrument> findByStatus(String status);

	List<Instrument> findByCategory_Id(Long categoryId);

	List<Instrument> findByBrand_Id(Long brandId);

	long countByCategory_Id(Long categoryId);
}
