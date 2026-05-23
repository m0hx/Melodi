package com.ga.melodi.controller;

import com.ga.melodi.model.Instrument;
import com.ga.melodi.service.InstrumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

	private final InstrumentService instrumentService;

	@GetMapping
	public List<Instrument> getAllInstruments(
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false) String status) {
		return instrumentService.getAllInstruments(categoryId, brandId, status);
	}

	@GetMapping("/{instrumentId}")
	public Instrument getInstrumentById(@PathVariable Long instrumentId) {
		return instrumentService.getInstrumentById(instrumentId);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN')")
	public Instrument createInstrument(@RequestBody Instrument instrumentObject) {
		return instrumentService.createInstrument(instrumentObject);
	}

	@PutMapping("/{instrumentId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Instrument updateInstrument(@PathVariable Long instrumentId, @RequestBody Instrument instrumentObject) {
		return instrumentService.updateInstrument(instrumentId, instrumentObject);
	}

	@DeleteMapping("/{instrumentId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public void deleteInstrument(@PathVariable Long instrumentId) {
		instrumentService.deleteInstrument(instrumentId);
	}

	@PutMapping("/{instrumentId}/image")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> updateInstrumentImage(
			@PathVariable Long instrumentId, @RequestParam("image") MultipartFile image) throws Exception {
		instrumentService.updateInstrumentImage(instrumentId, image);
		return ResponseEntity.ok("Instrument image updated successfully");
	}

	@GetMapping("/{instrumentId}/image")
	public ResponseEntity<byte[]> getInstrumentImage(@PathVariable Long instrumentId) {
		Instrument instrument = instrumentService.getInstrumentForImage(instrumentId);
		if (instrument.getImageData() == null || instrument.getImageData().length == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
				.header("Content-Type", instrument.getImageType() != null ? instrument.getImageType() : "image/jpeg")
				.body(instrument.getImageData());
	}

	@DeleteMapping("/{instrumentId}/image")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> removeInstrumentImage(@PathVariable Long instrumentId) {
		instrumentService.removeInstrumentImage(instrumentId);
		return ResponseEntity.ok("Instrument image removed successfully");
	}
}
