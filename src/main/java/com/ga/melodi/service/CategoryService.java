package com.ga.melodi.service;

import com.ga.melodi.model.Category;
import com.ga.melodi.repository.CategoryRepository;
import com.ga.melodi.repository.InstrumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final InstrumentRepository instrumentRepository;

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	public Category getCategoryById(Long categoryId) {
		return categoryRepository
				.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + categoryId));
	}

	public Category createCategory(Category categoryObject) {
		categoryObject.setId(null);
		String name = normalizeName(categoryObject.getName());
		categoryRepository
				.findByNameIgnoreCase(name)
				.ifPresent(c -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
				});
		categoryObject.setName(name);
		return categoryRepository.save(categoryObject);
	}

	public Category updateCategory(Long categoryId, Category categoryObject) {
		Category existing = getCategoryById(categoryId);
		String name = normalizeName(categoryObject.getName());
		categoryRepository
				.findByNameIgnoreCase(name)
				.filter(c -> !c.getId().equals(categoryId))
				.ifPresent(c -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
				});
		existing.setName(name);
		existing.setDescription(categoryObject.getDescription());
		existing.setImagePath(categoryObject.getImagePath());
		return categoryRepository.save(existing);
	}

	public void deleteCategory(Long categoryId) {
		getCategoryById(categoryId);
		if (instrumentRepository.countByCategory_Id(categoryId) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete category that is assigned to instruments");
		}
		categoryRepository.deleteById(categoryId);
	}

	private static String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
		}
		return name.trim();
	}
}
