package com.ga.melodi.controller;

import com.ga.melodi.model.Brand;
import com.ga.melodi.service.BrandService;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

	private final BrandService brandService;

	@GetMapping
	public List<Brand> getAllBrands() {
		return brandService.getAllBrands();
	}

	@GetMapping("/{brandId}")
	public Brand getBrandById(@PathVariable Long brandId) {
		return brandService.getBrandById(brandId);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN')")
	public Brand createBrand(@RequestBody Brand brandObject) {
		return brandService.createBrand(brandObject);
	}

	@PutMapping("/{brandId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Brand updateBrand(@PathVariable Long brandId, @RequestBody Brand brandObject) {
		return brandService.updateBrand(brandId, brandObject);
	}

	@DeleteMapping("/{brandId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public void deleteBrand(@PathVariable Long brandId) {
		brandService.deleteBrand(brandId);
	}
}
