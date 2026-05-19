package com.ga.melodi.init;

import com.ga.melodi.model.Brand;
import com.ga.melodi.model.Category;
import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.Role;
import com.ga.melodi.model.User;
import com.ga.melodi.repository.BrandRepository;
import com.ga.melodi.repository.CategoryRepository;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.RoleRepository;
import com.ga.melodi.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

	private record SeedInstrument(
			String name,
			String description,
			String category,
			String brand,
			String purchasePrice,
			String rentalPerDay,
			int purchaseStock,
			int rentalStock,
			String condition,
			String status) {}

	@Bean
	CommandLineRunner seedDatabase(
			RoleRepository roleRepository,
			UserRepository userRepository,
			CategoryRepository categoryRepository,
			BrandRepository brandRepository,
			InstrumentRepository instrumentRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			seedUsers(roleRepository, userRepository, passwordEncoder);
			seedCategories(categoryRepository);
			seedBrands(brandRepository);
			seedInstruments(categoryRepository, brandRepository, instrumentRepository);
		};
	}

	private static void seedUsers(
			RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		if (userRepository.count() > 0) {
			System.out.println("Users already seeded — skipping demo users.");
			return;
		}
		System.out.println("Seeding demo users...");
		Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
		Role userRole = roleRepository.findByName("USER").orElseThrow();

		User admin = new User();
		admin.setEmail("admin@melodi.local");
		admin.setPasswordHash(passwordEncoder.encode("admin123"));
		admin.setFullName("Melodi Admin");
		admin.setRole(adminRole);
		admin.setEmailVerifiedAt(Instant.now());
		userRepository.save(admin);

		User demo = new User();
		demo.setEmail("demo@melodi.local");
		demo.setPasswordHash(passwordEncoder.encode("demo123"));
		demo.setFullName("Demo User");
		demo.setRole(userRole);
		demo.setEmailVerifiedAt(Instant.now());
		userRepository.save(demo);

		System.out.println("✓ Admin: admin@melodi.local / admin123");
		System.out.println("✓ Demo:  demo@melodi.local / demo123");
	}

	private static void seedCategories(CategoryRepository categoryRepository) {
		// Category = instrument type. Covers a full band + orchestra/school + studio.
		List<String> names = List.of(
				"Guitars",
				"Basses",
				"Keys & Synth",
				"Drums",
				"String Instruments",
				"Wind Instruments",
				"Brass & Woodwinds",
				"Amps & Speakers",
				"Vocals & PA",
				"DJ & Electronic",
				"Accessories");
		for (String name : names) {
			if (categoryRepository.findByNameIgnoreCase(name).isEmpty()) {
				Category c = new Category();
				c.setName(name);
				categoryRepository.save(c);
			}
		}
	}

	private static void seedBrands(BrandRepository brandRepository) {
		record SeedBrand(String name, String country) {}

		List<SeedBrand> brands = List.of(
				new SeedBrand("Yamaha", "Japan"),
				new SeedBrand("Kawai", "Japan"),
				new SeedBrand("Hohner", "Germany"),
				new SeedBrand("Fender", "USA"),
				new SeedBrand("Squier", "USA"),
				new SeedBrand("Gibson", "USA"),
				new SeedBrand("Epiphone", "USA"),
				new SeedBrand("Ibanez", "Japan"),
				new SeedBrand("Taylor", "USA"),
				new SeedBrand("Martin", "USA"),
				new SeedBrand("Akai", "Japan"),
				new SeedBrand("Alesis", "USA"),
				new SeedBrand("Roland", "Japan"),
				new SeedBrand("Korg", "Japan"),
				new SeedBrand("Casio", "Japan"),
				new SeedBrand("Boss", "Japan"),
				new SeedBrand("Marshall", "UK"),
				new SeedBrand("Pearl", "Japan"),
				new SeedBrand("Ludwig", "USA"),
				new SeedBrand("Zildjian", "USA"),
				new SeedBrand("Shure", "USA"),
				new SeedBrand("Audio-Technica", "Japan"),
				new SeedBrand("Pioneer DJ", "Japan"),
				new SeedBrand("Selmer", "France"),
				new SeedBrand("Bach", "USA"),
				new SeedBrand("Stentor", "UK"),
				new SeedBrand("D'Addario", "USA"),
				new SeedBrand("Ernie Ball", "USA"),
				new SeedBrand("On-Stage", "USA"),
				new SeedBrand("Vic Firth", "USA"),
				new SeedBrand("Moog", "USA"),
				new SeedBrand("Arturia", "France"),
				new SeedBrand("Ampeg", "USA"),
				new SeedBrand("Orange", "UK"),
				new SeedBrand("JBL", "USA"),
				new SeedBrand("Mackie", "USA"),
				new SeedBrand("Nord", "Sweden"),
				new SeedBrand("Kala", "USA"));

		for (SeedBrand b : brands) {
			if (brandRepository.findByNameIgnoreCase(b.name()).isEmpty()) {
				Brand brand = new Brand();
				brand.setName(b.name());
				brand.setCountry(b.country());
				brandRepository.save(brand);
			}
		}
	}

	private static void seedInstruments(
			CategoryRepository categoryRepository,
			BrandRepository brandRepository,
			InstrumentRepository instrumentRepository) {
		List<SeedInstrument> seeds = List.of(
				new SeedInstrument(
						"Hohner Billy Joel Signature Harmonica (Key of C)",
						"Signature diatonic harmonica (model M535016) with engraved stainless cover plates, doussie comb, "
								+ "20 brass reeds, and display case. Official Billy Joel edition in C major.",
						"Wind Instruments",
						"Hohner",
						"49.99",
						"5.00",
						25,
						10,
						"NEW", "DISCONTINUED"),
				new SeedInstrument(
						"Yamaha P-45 88-Key Digital Piano (Black)",
						"Portable 88-key weighted GHS action digital piano with Advanced Wave Memory stereo sampling, "
								+ "10 voices, dual mode, USB to host, and included sustain pedal. Model P-45/B.",
						"Keys & Synth",
						"Yamaha",
						"429.99",
						"35.00",
						12,
						4,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Kawai GL-10 5' Baby Grand Piano (Polished Ebony)",
						"5'0\" baby grand with Millennium III action (ABS-Carbon), solid spruce soundboard, "
								+ "soft-close fallboard, and 10-year manufacturer warranty. MSRP reference ~$18,995.",
						"Keys & Synth",
						"Kawai",
						"17995.00",
						"0.00",
						2,
						0,
						"NEW", "HIDDEN"),
				new SeedInstrument(
						"Fender Player Stratocaster (SSS, Maple Fingerboard)",
						"Mexico-made Player Series Strat with three Player Series alnico 5 single-coils, "
								+ "2-point synchronized tremolo, and modern C neck profile.",
						"Guitars",
						"Fender",
						"799.99",
						"40.00",
						8,
						3,
						"NEW", "UNAVAILABLE"),
				new SeedInstrument(
						"Squier Affinity Series Stratocaster (Laurel Fingerboard)",
						"Entry-level Strat-style electric with three single-coil pickups, synchronous tremolo, "
								+ "and comfortable C-shaped neck — ideal first electric guitar.",
						"Guitars",
						"Squier",
						"229.99",
						"18.00",
						15,
						6,
						"USED", "DISCONTINUED"),
				new SeedInstrument(
						"Gibson Les Paul Studio (Smokehouse Burst)",
						"Mahogany body with maple cap, 490R/498T humbuckers, Grover tuners, "
								+ "and Studio weight-relieved design for modern rock and blues.",
						"Guitars",
						"Gibson",
						"1699.00",
						"55.00",
						4,
						2,
						"REFURBISHED", "HIDDEN"),
				new SeedInstrument(
						"Ibanez GRX70QA Electric Guitar (Transparent Red Burst)",
						"GRX series solid body with poplar body, maple neck, T106 tremolo, "
								+ "and PSND humbucker/single-coil pickup set.",
						"Guitars",
						"Ibanez",
						"249.99",
						"15.00",
						10,
						4,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Fender CD-60S Dreadnought Acoustic (Natural)",
						"Solid spruce top dreadnought with mahogany back and sides, rolled walnut fingerboard edges, "
								+ "and Scalloped X bracing for warm balanced tone.",
						"Guitars",
						"Fender",
						"229.99",
						"18.00",
						12,
						5,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha C40II Classical Guitar",
						"Full-size classical guitar with spruce top, meranti back/sides, nato neck, "
								+ "and 650mm scale — standard student and study instrument.",
						"Guitars",
						"Yamaha",
						"169.99",
						"12.00",
						14,
						6,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Ibanez AW54CE Acoustic-Electric (Open Pore Natural)",
						"Dreadnought cutaway with solid mahogany top, Ibanez AEQ-IT2 preamp with tuner, "
								+ "and warm open-pore finish.",
						"Guitars",
						"Ibanez",
						"399.99",
						"25.00",
						7,
						3,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Fender Champion 20 Guitar Combo Amp",
						"20W practice amp with 8\" Fender Special Design speaker, clean/voiced channels, "
								+ "and built-in reverb, delay, chorus, and tremolo effects.",
						"Amps & Speakers",
						"Fender",
						"129.99",
						"12.00",
						18,
						8,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Boss Katana 50 Gen 3 Guitar Combo Amp",
						"50W 1x12 combo with five amp characters, integrated effects, power control, "
								+ "and USB recording out — stage and practice ready.",
						"Amps & Speakers",
						"Boss",
						"299.99",
						"22.00",
						10,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Marshall MG15GFX 15W Guitar Combo Amp",
						"15W solid-state combo with 8\" speaker, four channels (Clean, Crunch, OD1, OD2), "
								+ "and built-in digital effects including reverb and chorus.",
						"Amps & Speakers",
						"Marshall",
						"119.99",
						"10.00",
						14,
						5,
						"USED", "DISCONTINUED"),
				new SeedInstrument(
						"D'Addario Assorted Guitar Pick Pack (10 pcs)",
						"Mixed gauge celluloid and nylon picks suitable for electric, acoustic, and classical practice.",
						"Accessories",
						"D'Addario",
						"8.99",
						"0.00",
						50,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Ernie Ball Polypro Guitar Strap (Black)",
						"2\" wide polypropylene strap with leather ends and adjustable length for electric and acoustic guitars.",
						"Accessories",
						"Ernie Ball",
						"12.99",
						"0.00",
						40,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha FC5 Compact Sustain Pedal",
						"Universal momentary sustain switch pedal compatible with Yamaha digital pianos and many keyboards.",
						"Accessories",
						"Yamaha",
						"44.99",
						"0.00",
						30,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"On-Stage KB8902B Keyboard Bench (Black)",
						"Padded X-style keyboard bench with height adjustment and non-slip rubber feet.",
						"Accessories",
						"On-Stage",
						"89.99",
						"0.00",
						20,
						0,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Akai Professional MPK Mini MK3",
						"25-key USB MIDI controller with velocity-sensitive keys, 8 backlit pads, "
								+ "4 assignable knobs, and included MPC Beats / Akai software bundle.",
						"Keys & Synth",
						"Akai",
						"99.00",
						"12.00",
						16,
						6,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Alesis V25 25-Key USB MIDI Controller",
						"25-key USB-MIDI controller with full-size keys, 8 velocity pads, 4 knobs, "
								+ "pitch/mod wheels, and production software included.",
						"Keys & Synth",
						"Alesis",
						"109.00",
						"10.00",
						14,
						5,
						"USED", "DISCONTINUED"),
				new SeedInstrument(
						"Roland FP-30X Digital Piano (Black)",
						"88-key SuperNATURAL piano with PHA-4 Standard keyboard, Bluetooth audio/MIDI, "
								+ "twin speakers, and compact furniture-style cabinet.",
						"Keys & Synth",
						"Roland",
						"899.99",
						"45.00",
						8,
						3,
						"REFURBISHED", "UNAVAILABLE"),
				new SeedInstrument(
						"D'Addario NS Tri-Action Capo (Silver)",
						"Micrometer tension capo for steel-string acoustic and electric guitars with even pressure and quick release.",
						"Accessories",
						"D'Addario",
						"14.99",
						"0.00",
						35,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Fender Professional Series Instrument Cable 10 ft",
						"10-foot braided instrument cable with 90% copper coverage and lifetime warranty — stage and studio use.",
						"Accessories",
						"Fender",
						"24.99",
						"0.00",
						45,
						0,
						"NEW", "AVAILABLE"),
				// —— more guitars ——
				new SeedInstrument(
						"Epiphone Les Paul Standard 60s (Bourbon Burst)",
						"Inspired by 1960s Gibson Les Paul Standards with ProBucker humbuckers, "
								+ "mahogany body with maple cap, and vintage-style tuners.",
						"Guitars",
						"Epiphone",
						"549.00",
						"32.00",
						9,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Taylor 214ce Grand Auditorium (Natural)",
						"Solid Sitka spruce top with layered rosewood back/sides, Expression System 2 pickup, "
								+ "venetian cutaway, and Taylor gig bag ready setup.",
						"Guitars",
						"Taylor",
						"1099.00",
						"48.00",
						5,
						2,
						"NEW", "HIDDEN"),
				new SeedInstrument(
						"Martin LX1E Little Martin (Natural)",
						"Compact 3/4-size acoustic-electric with Sitka spruce top, modified low-oval neck, "
								+ "and Fishman Sonitone electronics — great travel guitar.",
						"Guitars",
						"Martin",
						"499.00",
						"28.00",
						8,
						3,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Fender American Professional II Telecaster (Butterscotch Blonde)",
						"USA-made Tele with V-Mod II pickups, treble-bleed tone circuit, "
								+ "and deep C neck carve for modern players.",
						"Guitars",
						"Fender",
						"1699.99",
						"58.00",
						3,
						1,
						"NEW", "HIDDEN"),
				// —— more pianos & keyboards ——
				new SeedInstrument(
						"Casio PX-S1100 Digital Piano (Black)",
						"88-key slim digital piano with Smart Scaled Hammer Action, AiR sound source, "
								+ "Bluetooth MIDI/audio, and 18 built-in tones.",
						"Keys & Synth",
						"Casio",
						"649.99",
						"38.00",
						10,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Korg B2 Digital Piano (Black)",
						"88-key digital piano with NH keyboard action, 12 sounds including German and Italian grands, "
								+ "and lightweight 11.4 kg design.",
						"Keys & Synth",
						"Korg",
						"749.99",
						"40.00",
						8,
						3,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha PSR-E473 Portable Keyboard",
						"61-key portable arranger keyboard with 820 voices, 290 styles, "
								+ "USB audio interface, and lesson functions.",
						"Keys & Synth",
						"Yamaha",
						"299.99",
						"18.00",
						14,
						6,
						"USED", "DISCONTINUED"),
				// —— more wind ——
				new SeedInstrument(
						"Hohner Special 20 Harmonica (Key of G)",
						"Classic diatonic harmonica with recessed reed plates, plastic comb, "
								+ "and responsive draw/chord playing — industry standard blues harp.",
						"Wind Instruments",
						"Hohner",
						"54.99",
						"5.00",
						30,
						12,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha YFL-222 Student Flute",
						"Nickel-silver student flute with offset G, C footjoint, "
								+ "and durable keywork — common school band instrument.",
						"Wind Instruments",
						"Yamaha",
						"1099.00",
						"35.00",
						6,
						3,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Selmer Paris AS500 Alto Saxophone",
						"Student alto sax with yellow brass body, leather pads, "
								+ "and balanced keywork for beginning jazz and concert band players.",
						"Brass & Woodwinds",
						"Selmer",
						"1899.00",
						"45.00",
						4,
						2,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Bach TR300 Student Trumpet",
						"Beginner Bb trumpet with yellow brass bell, stainless steel pistons, "
								+ "and 7C mouthpiece — standard school band model.",
						"Brass & Woodwinds",
						"Bach",
						"799.00",
						"28.00",
						7,
						3,
						"USED", "AVAILABLE"),
				// —— drums & percussion ——
				new SeedInstrument(
						"Pearl Roadshow 5-Piece Drum Set (Bronze Metallic)",
						"Complete 5-piece shell pack with 9-ply poplar shells, hardware pack, "
								+ "cymbal stand, throne, kick pedal, and 16\" crash/14\" hi-hats included.",
						"Drums",
						"Pearl",
						"549.00",
						"35.00",
						6,
						2,
						"USED", "DISCONTINUED"),
				new SeedInstrument(
						"Ludwig Accent Drive 5-Piece Drum Set (Blue Metallic)",
						"5-piece kit with 9mm poplar shells, 22\" bass drum, hardware, "
								+ "snare, throne, and cymbals — entry complete drum set.",
						"Drums",
						"Ludwig",
						"499.00",
						"32.00",
						5,
						2,
						"USED", "DISCONTINUED"),
				new SeedInstrument(
						"Zildjian S Series Cymbal Starter Box Set",
						"Matched cymbal set with 14\" hi-hats, 16\" crash, and 20\" ride — "
								+ "B12 bronze S Series for versatile rock and pop.",
						"Drums",
						"Zildjian",
						"399.99",
						"22.00",
						12,
						5,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Vic Firth 5A Wood Tip Drumstick Pair (12-Pack)",
						"American hickory 5A sticks with wood tip — most popular general-purpose drumstick size.",
						"Drums",
						"Vic Firth",
						"89.99",
						"0.00",
						25,
						0,
						"NEW", "AVAILABLE"),
				// —— orchestral strings ——
				new SeedInstrument(
						"Stentor Student I Violin Outfit 4/4",
						"Full-size student violin outfit with carved solid spruce top, maple back, "
								+ "ebonized fingerboard, bow, case, and rosin included.",
						"String Instruments",
						"Stentor",
						"299.99",
						"18.00",
						10,
						4,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha Cello Model AVC5 (4/4)",
						"Student cello with spruce top, maple back and sides, "
								+ "and warm tone suitable for school orchestra programs.",
						"String Instruments",
						"Yamaha",
						"1899.00",
						"42.00",
						3,
						1,
						"USED", "AVAILABLE"),
				// —— studio & recording ——
				new SeedInstrument(
						"Shure SM58 Dynamic Vocal Microphone",
						"Industry-standard cardioid dynamic vocal mic with pneumatic shock mount, "
								+ "built-in spherical wind and pop filter — live and studio staple.",
						"Vocals & PA",
						"Shure",
						"99.00",
						"10.00",
						20,
						8,
						"REFURBISHED", "UNAVAILABLE"),
				new SeedInstrument(
						"Audio-Technica AT2020 Cardioid Condenser Microphone",
						"Side-address studio condenser with low-mass diaphragm for vocals and acoustic instruments — "
								+ "XLR output, 48V phantom powered.",
						"Vocals & PA",
						"Audio-Technica",
						"99.00",
						"10.00",
						18,
						6,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Shure SM57 Dynamic Instrument Microphone",
						"Cardioid dynamic microphone favored for guitar amps, snare drums, "
								+ "and brass — rugged tour-ready build.",
						"Vocals & PA",
						"Shure",
						"99.00",
						"10.00",
						22,
						8,
						"REFURBISHED", "AVAILABLE"),
				// —— DJ & electronic ——
				new SeedInstrument(
						"Pioneer DJ DDJ-FLX4 2-Channel DJ Controller",
						"Compact 2-deck Rekordbox and Serato compatible controller with built-in sound card, "
								+ "jog wheels, and USB bus power.",
						"DJ & Electronic",
						"Pioneer DJ",
						"299.00",
						"20.00",
						8,
						3,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Roland TR-06 Drumatix Drum Machine",
						"Compact recreation of the classic TR-606 with six analog-style voices, "
								+ "pattern sequencing, and battery/USB power.",
						"DJ & Electronic",
						"Roland",
						"399.99",
						"22.00",
						6,
						2,
						"REFURBISHED", "DISCONTINUED"),
				// —— more amps ——
				new SeedInstrument(
						"Boss Katana-100 MkII Guitar Combo Amp",
						"100W 1x12 combo with five amp types, 60+ effects, power control for home/stage, "
								+ "and line/phones/recording outputs.",
						"Amps & Speakers",
						"Boss",
						"449.99",
						"28.00",
						7,
						3,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Fender Rumble 40 v3 Bass Combo Amp",
						"40W bass combo with 10\" speaker, overdrive circuit, "
								+ "and lightweight design for practice and small gigs.",
						"Amps & Speakers",
						"Fender",
						"199.99",
						"15.00",
						12,
						5,
						"REFURBISHED", "AVAILABLE"),
				// —— more accessories ——
				new SeedInstrument(
						"On-Stage GS7462B Single Guitar Stand",
						"A-frame style stand with foam arms and security strap — fits acoustic and electric guitars.",
						"Accessories",
						"On-Stage",
						"24.99",
						"0.00",
						40,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Ernie Ball Regular Slinky Electric Guitar Strings (10-46)",
						"Nickel wound electric guitar string set 10-46 gauge — best-selling set in the USA.",
						"Accessories",
						"Ernie Ball",
						"6.99",
						"0.00",
						80,
						0,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha PKBX2 Double-Braced Keyboard Stand",
						"Adjustable X-style keyboard stand with double bracing for digital pianos and synthesizers.",
						"Accessories",
						"Yamaha",
						"79.99",
						"0.00",
						25,
						0,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"D'Addario Humidipak Automatic Humidity Control System",
						"Two-way humidity control for acoustic guitars — maintains 45-50% RH inside the case.",
						"Accessories",
						"D'Addario",
						"19.99",
						"0.00",
						30,
						0,
						"NEW", "AVAILABLE"),
				// —— basses (band rhythm section) ——
				new SeedInstrument(
						"Fender Player Precision Bass (Polar White)",
						"Mexico-made P-Bass with Player Series split-coil pickup, 4-saddle bridge, "
								+ "and modern C neck — classic rock, pop, and funk foundation.",
						"Basses",
						"Fender",
						"849.99",
						"42.00",
						7,
						3,
						"NEW", "UNAVAILABLE"),
				new SeedInstrument(
						"Fender Player Jazz Bass (3-Color Sunburst)",
						"Dual single-coil Jazz Bass pickups, slim neck, and versatile tone for jazz, rock, and R&B.",
						"Basses",
						"Fender",
						"899.99",
						"44.00",
						6,
						3,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Squier Affinity Series Jazz Bass (Black)",
						"Affordable Jazz Bass with two single-coils and comfortable C-shape neck — ideal first bass guitar.",
						"Basses",
						"Squier",
						"249.99",
						"18.00",
						12,
						5,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Ibanez SR300E Soundgear (Weathered Black)",
						"4-string Soundgear with PowerSpan pickups, lightweight body, and fast neck for modern metal and funk.",
						"Basses",
						"Ibanez",
						"399.99",
						"25.00",
						9,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Epiphone Thunderbird Vintage Pro Bass (Natural)",
						"Reverse-body Thunderbird with T-Pro humbuckers and 1960s-inspired design for classic rock tone.",
						"Basses",
						"Epiphone",
						"599.00",
						"32.00",
						5,
						2,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Ampeg BA-210v2 Bass Combo Amp",
						"450W 2x10 bass combo with scrambler overdrive, 3-band EQ, and Ampeg classic tone stack.",
						"Amps & Speakers",
						"Ampeg",
						"499.99",
						"28.00",
						8,
						3,
						"REFURBISHED", "AVAILABLE"),
				// —— synthesizers & stage keys ——
				new SeedInstrument(
						"Korg minilogue xd Polyphonic Analog Synthesizer",
						"37-key analog synth with digital multi-engine, vocoder, motion sequencer, "
								+ "and 500 user programs — flagship indie/electronic synth.",
						"Keys & Synth",
						"Korg",
						"649.99",
						"38.00",
						6,
						2,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Roland JD-Xi Interactive Analog/Digital Crossover Synthesizer",
						"37 mini-key synth with analog filter, digital voices, vocoder, and pattern sequencer.",
						"Keys & Synth",
						"Roland",
						"499.99",
						"30.00",
						7,
						3,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Arturia MicroFreak Hybrid Paraphonic Synthesizer",
						"25-key digital oscillator synth with analog filter, aftertouch plate, "
								+ "and modular-style flexibility in a compact package.",
						"Keys & Synth",
						"Arturia",
						"349.00",
						"25.00",
						8,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Moog Mother-32 Semi-Modular Analog Synthesizer",
						"Single-voice semi-modular synth with Moog ladder filter, 32-step sequencer, "
								+ "and patch bay for Eurorack integration.",
						"Keys & Synth",
						"Moog",
						"599.00",
						"35.00",
						4,
						2,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha Reface DX FM Synthesizer",
						"37-key FM synth inspired by the DX7 with built-in speakers, battery power, "
								+ "and classic 1980s electric piano/bell tones.",
						"Keys & Synth",
						"Yamaha",
						"499.99",
						"28.00",
						6,
						2,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Nord Stage 4 88 Digital Stage Piano",
						"Professional 88-key stage keyboard with triple sensor keybed, piano/organ/synth sections, "
								+ "and seamless live performance workflow.",
						"Keys & Synth",
						"Nord",
						"5499.00",
						"0.00",
						2,
						0,
						"NEW", "HIDDEN"),
				// —— band drums & electronic kit ——
				new SeedInstrument(
						"Roland V-Drums TD-07KV Electronic Drum Kit",
						"Compact electronic kit with mesh snare/toms, kick tower, dual-zone crash/ride, "
								+ "and TD-07 sound module with Bluetooth.",
						"Drums",
						"Roland",
						"999.99",
						"45.00",
						4,
						2,
						"REFURBISHED", "UNAVAILABLE"),
				new SeedInstrument(
						"Pearl Export EXX725S 5-Piece Drum Set (Pure White)",
						"Best-selling Export series with poplar shells, 22\" kick, hardware, cymbals, and throne included.",
						"Drums",
						"Pearl",
						"899.99",
						"48.00",
						5,
						2,
						"REFURBISHED", "UNAVAILABLE"),
				new SeedInstrument(
						"Snare Drum Stand + Practice Pad Bundle",
						"Double-braced snare stand with 12\" rubber practice pad — band rehearsal essential.",
						"Drums",
						"On-Stage",
						"49.99",
						"0.00",
						20,
						0,
						"NEW", "AVAILABLE"),
				// —— vocals & live PA ——
				new SeedInstrument(
						"JBL EON710 Powered PA Speaker",
						"10\" 1300W powered loudspeaker with Bluetooth, DSP presets, "
								+ "and tripod mount — small band PA and vocals.",
						"Vocals & PA",
						"JBL",
						"399.00",
						"25.00",
						10,
						4,
						"NEW", "UNAVAILABLE"),
				new SeedInstrument(
						"Mackie Thump215 15\" Powered Loudspeaker",
						"1300W 15\" powered speaker with Class-D amp and built-in mixer — live vocals and keys.",
						"Vocals & PA",
						"Mackie",
						"699.00",
						"35.00",
						6,
						3,
						"NEW", "AVAILABLE"),
				new SeedInstrument(
						"Shure BLX288/PG58 Wireless Vocal System",
						"Dual-channel wireless mic system with two PG58 handheld transmitters — band vocals and MC.",
						"Vocals & PA",
						"Shure",
						"599.00",
						"32.00",
						5,
						2,
						"REFURBISHED", "AVAILABLE"),
				// —— more guitars & strings for band ——
				new SeedInstrument(
						"Kala KA-15S Soprano Ukulele (Satin Mahogany)",
						"Soprano ukulele with mahogany top/back/sides, walnut fingerboard, "
								+ "and Aquila strings — songwriting and classroom favorite.",
						"Guitars",
						"Kala",
						"79.99",
						"8.00",
						20,
						8,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Orange Crush 35RT Guitar Combo Amp",
						"35W 1x10 combo with reverb, tuner, and headphone out — iconic British rock tone in a practice amp.",
						"Amps & Speakers",
						"Orange",
						"269.99",
						"18.00",
						10,
						4,
						"REFURBISHED", "AVAILABLE"),
				new SeedInstrument(
						"Yamaha YCL-255 Student Clarinet",
						"ABS resin student clarinet with Valentino pads and durable keywork — standard concert band clarinet.",
						"Brass & Woodwinds",
						"Yamaha",
						"899.00",
						"28.00",
						6,
						3,
						"USED", "AVAILABLE"),
				new SeedInstrument(
						"Stentor Student II Viola Outfit 15\"",
						"15\" viola outfit with solid wood construction, bow, case, and rosin — orchestra and school programs.",
						"String Instruments",
						"Stentor",
						"349.99",
						"20.00",
						8,
						3,
						"USED", "AVAILABLE"));

		for (SeedInstrument s : seeds) {
			Category category = categoryRepository
					.findByNameIgnoreCase(s.category())
					.orElseThrow(() -> new IllegalStateException("Category not seeded: " + s.category()));
			Brand brand = brandRepository
					.findByNameIgnoreCase(s.brand())
					.orElseThrow(() -> new IllegalStateException("Brand not seeded: " + s.brand()));

			// Consumables (Accessories category) are purchase-only.
			boolean purchaseOnly = "Accessories".equals(s.category());
			int rentalStock = purchaseOnly ? 0 : s.rentalStock();
			BigDecimal rentalPrice = purchaseOnly ? BigDecimal.ZERO : new BigDecimal(s.rentalPerDay());
			var existing = instrumentRepository.findByNameIgnoreCase(s.name());
			if (existing.isPresent()) {
				Instrument instrument = existing.get();
				instrument.setRentalStock(rentalStock);
				instrument.setRentalPricePerDay(rentalPrice);
				instrument.setCondition(s.condition());
				instrument.setStatus(s.status());
				instrumentRepository.save(instrument);
				continue;
			}

			Instrument instrument = new Instrument();
			instrument.setName(s.name());
			instrument.setDescription(s.description());
			instrument.setCategory(category);
			instrument.setBrand(brand);
			instrument.setPurchasePrice(new BigDecimal(s.purchasePrice()));
			instrument.setRentalPricePerDay(rentalPrice);
			instrument.setPurchaseStock(s.purchaseStock());
			instrument.setRentalStock(rentalStock);
			instrument.setCondition(s.condition());
			instrument.setStatus(s.status());
			instrumentRepository.save(instrument);
		}

		System.out.println("✓ Catalog seed check complete (" + instrumentRepository.count() + " instruments in database).");
	}

}
