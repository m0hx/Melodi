package com.ga.melodi.repository;

import com.ga.melodi.model.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrder_Id(Long orderId);

	Optional<Payment> findByPaymentReference(String paymentReference);
}
