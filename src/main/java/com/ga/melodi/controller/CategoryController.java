package com.ga.melodi.controller;

import com.ga.melodi.model.Category;
import com.ga.melodi.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping
	public List<Category> getAllCategories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/{categoryId}")
	public Category getCategoryById(@PathVariable Long categoryId) {
		return categoryService.getCategoryById(categoryId);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN')")
	public Category createCategory(@RequestBody Category categoryObject) {
		return categoryService.createCategory(categoryObject);
	}

	@PutMapping("/{categoryId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Category updateCategory(@PathVariable Long categoryId, @RequestBody Category categoryObject) {
		return categoryService.updateCategory(categoryId, categoryObject);
	}

	@DeleteMapping("/{categoryId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public void deleteCategory(@PathVariable Long categoryId) {
		categoryService.deleteCategory(categoryId);
	}
}
