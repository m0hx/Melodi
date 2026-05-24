package com.ga.melodi.init;

import com.ga.melodi.model.Instrument;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/** Loads demo product photos from {@code classpath:seed/instruments/}. */
final class InstrumentSeedImages {

	private static final String SEED_DIR = "seed/instruments/";

	/** Demo catalog only — add entries here when you drop more files in seed/instruments/. */
	private static final Map<String, String> DEMO_IMAGE_FILES = Map.of(
			"Hohner Billy Joel Signature Harmonica (Key of C)",
					"hohner-billy-joel-signature-harmonica-(key-of-c).jpg",
			"Yamaha P-45 88-Key Digital Piano (Black)",
					"yamaha-p-45-88-key-digital-piano-(black).jpg",
			"Fender Player Stratocaster (SSS, Maple Fingerboard)",
					"fender-player-stratocaster-(sss-maple-fingerboard).jpg",
			"Ibanez GRX70QA Electric Guitar (Transparent Red Burst)",
					"ibanez-grx70qa-electric-guitar.png",
			"Akai Professional MPK Mini MK3",
					"akai-professional-mpk-mini-mk3.jpg",
			"Alesis V25 25-Key USB MIDI Controller",
					"alesis-v25-25-key-usb-midi-controller.jpg");

	private InstrumentSeedImages() {}

	/** Sets image fields when {@code imageData} is empty and a demo file exists. */
	static boolean applyIfMissing(Instrument instrument) {
		if (instrument.getImageData() != null && instrument.getImageData().length > 0) {
			return false;
		}
		String filename = DEMO_IMAGE_FILES.get(instrument.getName());
		if (filename == null) {
			return false;
		}
		String path = SEED_DIR + filename;
		ClassPathResource resource = new ClassPathResource(path);
		if (!resource.exists()) {
			return false;
		}
		try (InputStream in = resource.getInputStream()) {
			byte[] data = in.readAllBytes();
			instrument.setImageName(filename);
			instrument.setImageType(mimeType(filename));
			instrument.setImageData(data);
			return true;
		} catch (IOException e) {
			System.err.println("Failed to load demo image " + path + ": " + e.getMessage());
			return false;
		}
	}

	private static String mimeType(String filename) {
		String lower = filename.toLowerCase();
		if (lower.endsWith(".png")) {
			return "image/png";
		}
		if (lower.endsWith(".webp")) {
			return "image/webp";
		}
		return "image/jpeg";
	}
}
