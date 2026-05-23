package com.ga.melodi.service;

import com.ga.melodi.model.Instrument;
import com.ga.melodi.model.Order;
import com.ga.melodi.model.OrderItem;
import com.ga.melodi.model.Rental;
import com.ga.melodi.model.User;
import com.ga.melodi.repository.InstrumentRepository;
import com.ga.melodi.repository.RentalRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RentalService {

	private final RentalRepository rentalRepository;
	private final InstrumentRepository instrumentRepository;
	private final CurrentUserService currentUserService;

	@Transactional
	public void createRentalsForConfirmedOrder(Order order, List<OrderItem> items) {
		if (!"CONFIRMED".equals(order.getStatus())) {
			return;
		}
		User user = order.getUser();
		for (OrderItem item : items) {
			if (!"RENT".equals(item.getMode())) {
				continue;
			}
			if (rentalRepository.existsByOrderItem_Id(item.getId())) {
				continue;
			}
			Rental rental = new Rental();
			rental.setOrderItem(item);
			rental.setUser(user);
			rental.setInstrument(item.getInstrument());
			rental.setStartsAt(item.getRentalStartDate());
			rental.setEndsAt(item.getRentalEndDate());
			rental.setStatus(resolveInitialStatus(item.getRentalEndDate()));
			rentalRepository.save(rental);
		}
	}

	@Transactional
	public List<Rental> listMyRentals() {
		User user = currentUserService.getCurrentUser();
		List<Rental> rentals = rentalRepository.findByUser_IdOrderByStartsAtDesc(user.getId());
		for (Rental rental : rentals) {
			if (markOverdueIfNeeded(rental)) {
				rentalRepository.save(rental);
			}
		}
		return rentals;
	}

	@Transactional
	public List<Rental> listAllRentals() {
		List<Rental> rentals = rentalRepository.findAllByOrderByStartsAtDesc();
		for (Rental rental : rentals) {
			if (markOverdueIfNeeded(rental)) {
				rentalRepository.save(rental);
			}
		}
		return rentals;
	}

	@Transactional
	public Rental returnRental(Long rentalId) {
		User user = currentUserService.getCurrentUser();
		Rental rental = rentalRepository
				.findByIdAndUser_Id(rentalId, user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));

		if ("RETURNED".equals(rental.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental already returned");
		}
		if ("CANCELLED".equals(rental.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental was cancelled");
		}

		Instrument instrument = instrumentRepository
				.findById(rental.getInstrument().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument not found"));

		int quantity = rental.getOrderItem().getQuantity();
		instrument.setRentalStock(instrument.getRentalStock() + quantity);
		instrumentRepository.save(instrument);

		rental.setReturnedAt(Instant.now());
		rental.setStatus("RETURNED");
		return rentalRepository.save(rental);
	}

	@Transactional
	public Rental returnRentalAsAdmin(Long rentalId) {
		Rental rental = rentalRepository
				.findById(rentalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));

		if ("RETURNED".equals(rental.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental already returned");
		}
		if ("CANCELLED".equals(rental.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental was cancelled");
		}

		Instrument instrument = instrumentRepository
				.findById(rental.getInstrument().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument not found"));

		int quantity = rental.getOrderItem().getQuantity();
		instrument.setRentalStock(instrument.getRentalStock() + quantity);
		instrumentRepository.save(instrument);

		rental.setReturnedAt(Instant.now());
		rental.setStatus("RETURNED");
		return rentalRepository.save(rental);
	}

	private boolean markOverdueIfNeeded(Rental rental) {
		if (!"ACTIVE".equals(rental.getStatus())) {
			return false;
		}
		if (rental.getEndsAt() != null && rental.getEndsAt().isBefore(Instant.now())) {
			rental.setStatus("OVERDUE");
			return true;
		}
		return false;
	}

	private String resolveInitialStatus(Instant endsAt) {
		if (endsAt != null && endsAt.isBefore(Instant.now())) {
			return "OVERDUE";
		}
		return "ACTIVE";
	}
}
