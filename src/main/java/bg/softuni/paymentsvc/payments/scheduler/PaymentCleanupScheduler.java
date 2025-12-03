package bg.softuni.paymentsvc.payments.scheduler;

import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCleanupScheduler {

    private final PaymentRepository paymentRepository;

    @CacheEvict(value = "payments", allEntries = true)
    @Scheduled(cron = "0 0 * * * *")
    public void failOldPendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);

        List<Payment> oldPending = paymentRepository
                .findAllByStatusAndCreatedOnBefore(PaymentStatus.PENDING, cutoff);

        if (oldPending.isEmpty()) {
            log.debug("No pending payments older than 2 hours found.");
            return;
        }

        log.info("Marking {} payments as FAILED (older than 2 hours)", oldPending.size());

        LocalDateTime now = LocalDateTime.now();
        oldPending.forEach(p -> {
            p.setStatus(PaymentStatus.FAILED);
            p.setUpdatedOn(now);
        });

        paymentRepository.saveAll(oldPending);
    }
}
