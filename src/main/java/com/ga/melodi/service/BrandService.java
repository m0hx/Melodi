package com.ga.melodi.service;

import com.ga.melodi.model.Brand;
import com.ga.melodi.repository.BrandRepository;
import com.ga.melodi.repository.InstrumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BrandService {

	private final BrandRepository brandRepository;
	private final InstrumentRepository instrumentRepository;

	public List<Brand> getAllBrands() {
		return brandRepository.findAll();
	}

	public Brand getBrandById(Long brandId) {
		return brandRepository
				.findById(brandId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + brandId));
	}

	public Brand createBrand(Brand brandObject) {
		brandObject.setId(null);
		String name = normalizeName(brandObject.getName());
		brandRepository
				.findByNameIgnoreCase(name)
				.ifPresent(b -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand name already exists");
				});
		brandObject.setName(name);
		return brandRepository.save(brandObject);
	}

	public Brand updateBrand(Long brandId, Brand brandObject) {
		Brand existing = getBrandById(brandId);
		String name = normalizeName(brandObject.getName());
		brandRepository
				.findByNameIgnoreCase(name)
				.filter(b -> !b.getId().equals(brandId))
				.ifPresent(b -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand name already exists");
				});
		existing.setName(name);
		existing.setCountry(brandObject.getCountry());
		existing.setDescription(brandObject.getDescription());
		return brandRepository.save(existing);
	}

	public void deleteBrand(Long brandId) {
		getBrandById(brandId);
		if (instrumentRepository.countByBrand_Id(brandId) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete brand that is assigned to instruments");
		}
		brandRepository.deleteById(brandId);
	}

	private static String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name is required");
		}
		return name.trim();
	}
}
