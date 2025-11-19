package bg.softuni.paymentsvc.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import bg.softuni.paymentsvc.payments.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
