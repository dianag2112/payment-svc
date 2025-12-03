package bg.softuni.paymentsvc.payments.scheduler;

import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCleanupSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentCleanupScheduler scheduler;

    @Test
    void failOldPendingPayments_shouldMarkOldPaymentsAsFailed() {
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

        Payment oldPending = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .amount(BigDecimal.TEN)
                .status(PaymentStatus.PENDING)
                .method("CARD")
                .createdOn(threeHoursAgo)
                .updatedOn(threeHoursAgo)
                .build();

        when(paymentRepository.findAllByStatusAndCreatedOnBefore(
                eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(oldPending));

        scheduler.failOldPendingPayments();

        ArgumentCaptor<List<Payment>> captor = ArgumentCaptor.forClass(List.class);
        verify(paymentRepository).saveAll(captor.capture());

        List<Payment> saved = captor.getValue();
        assertEquals(1, saved.size());

        Payment updated = saved.get(0);
        assertEquals(PaymentStatus.FAILED, updated.getStatus());
    }

    @Test
    void failOldPendingPayments_shouldDoNothing_whenNoneFound() {
        when(paymentRepository.findAllByStatusAndCreatedOnBefore(
                eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.failOldPendingPayments();

        verify(paymentRepository, never()).saveAll(any());
    }
}
