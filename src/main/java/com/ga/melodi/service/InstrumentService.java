package com.ga.melodi.service;

import com.ga.melodi.model.Brand;
import com.ga.melodi.model.Category;
import com.ga.melodi.model.Instrument;
import com.ga.melodi.repository.BrandRepository;
import com.ga.melodi.repository.CategoryRepository;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.OrderItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InstrumentService {

	private final InstrumentRepository instrumentRepository;
	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final OrderItemRepository orderItemRepository;

	public List<Instrument> getAllInstruments(Long categoryId, Long brandId, String status) {
		if (categoryId != null) {
			return instrumentRepository.findByCategory_Id(categoryId);
		}
		if (brandId != null) {
			return instrumentRepository.findByBrand_Id(brandId);
		}
		if (status != null && !status.isBlank()) {
			return instrumentRepository.findByStatus(status.trim());
		}
		return instrumentRepository.findAll();
	}

	public Instrument getInstrumentById(Long instrumentId) {
		return instrumentRepository
				.findById(instrumentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found with id: " + instrumentId));
	}

	public Instrument createInstrument(Instrument instrumentObject) {
		instrumentObject.setId(null);
		validateInstrumentFields(instrumentObject);
		instrumentObject.setCategory(resolveCategory(instrumentObject.getCategory().getId()));
		instrumentObject.setBrand(resolveBrand(instrumentObject.getBrand().getId()));
		return instrumentRepository.save(instrumentObject);
	}

	public Instrument updateInstrument(Long instrumentId, Instrument instrumentObject) {
		Instrument existing = getInstrumentById(instrumentId);
		validateInstrumentFields(instrumentObject);
		existing.setName(instrumentObject.getName().trim());
		existing.setDescription(instrumentObject.getDescription());
		existing.setPurchasePrice(instrumentObject.getPurchasePrice());
		existing.setRentalPricePerDay(instrumentObject.getRentalPricePerDay());
		existing.setPurchaseStock(instrumentObject.getPurchaseStock());
		existing.setRentalStock(instrumentObject.getRentalStock());
		existing.setCondition(instrumentObject.getCondition());
		existing.setStatus(instrumentObject.getStatus());
		if (instrumentObject.getCategory() != null && instrumentObject.getCategory().getId() != null) {
			existing.setCategory(resolveCategory(instrumentObject.getCategory().getId()));
		}
		if (instrumentObject.getBrand() != null && instrumentObject.getBrand().getId() != null) {
			existing.setBrand(resolveBrand(instrumentObject.getBrand().getId()));
		}
		return instrumentRepository.save(existing);
	}

	public void deleteInstrument(Long instrumentId) {
		getInstrumentById(instrumentId);
		if (orderItemRepository.countByInstrument_Id(instrumentId) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete instrument that has order history");
		}
		instrumentRepository.deleteById(instrumentId);
	}

	private Category resolveCategory(Long categoryId) {
		return categoryRepository
				.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category id"));
	}

	private Brand resolveBrand(Long brandId) {
		return brandRepository
				.findById(brandId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid brand id"));
	}

	private static void validateInstrumentFields(Instrument instrument) {
		if (instrument.getName() == null || instrument.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument name is required");
		}
		if (instrument.getCategory() == null || instrument.getCategory().getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
		}
		if (instrument.getBrand() == null || instrument.getBrand().getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required");
		}
		if (instrument.getPurchasePrice() == null || instrument.getPurchasePrice().signum() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase price must be positive");
		}
		if (instrument.getRentalPricePerDay() == null || instrument.getRentalPricePerDay().signum() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental price per day must be positive");
		}
		if (instrument.getPurchaseStock() == null || instrument.getPurchaseStock() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase stock cannot be negative");
		}
		if (instrument.getRentalStock() == null || instrument.getRentalStock() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental stock cannot be negative");
		}
	}
}
