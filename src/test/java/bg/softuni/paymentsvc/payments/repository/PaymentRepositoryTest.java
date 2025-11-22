package bg.softuni.paymentsvc.payments.repository;

import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("findByOrderId should return payment when exists")
    void findByOrderId_shouldReturnPayment_whenExists() {
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(BigDecimal.valueOf(9.90))
                .status(PaymentStatus.PENDING)
                .method("CARD")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        Optional<Payment> result = paymentRepository.findByOrderId(orderId);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("findAllByStatusAndCreatedOnBefore should return old pending payments")
    void findAllByStatusAndCreatedOnBefore_shouldReturnOldPendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursAgo = now.minusHours(3);

        Payment oldPending = Payment.builder()
                .orderId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(5.00))
                .status(PaymentStatus.PENDING)
                .method("CARD")
                .createdOn(threeHoursAgo)
                .updatedOn(threeHoursAgo)
                .build();

        Payment recentPending = Payment.builder()
                .orderId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(7.00))
                .status(PaymentStatus.PENDING)
                .method("CARD")
                .createdOn(now.minusMinutes(30))
                .updatedOn(now.minusMinutes(30))
                .build();

        paymentRepository.save(oldPending);
        paymentRepository.save(recentPending);

        List<Payment> result = paymentRepository
                .findAllByStatusAndCreatedOnBefore(PaymentStatus.PENDING, now.minusHours(2));

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Payment::getId)
                .isEqualTo(oldPending.getId());
    }
}
