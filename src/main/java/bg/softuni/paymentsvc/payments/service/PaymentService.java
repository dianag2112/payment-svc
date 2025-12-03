package bg.softuni.paymentsvc.payments.service;

import bg.softuni.paymentsvc.payments.exception.PaymentAlreadyExistsException;
import bg.softuni.paymentsvc.payments.exception.PaymentNotFoundException;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
import bg.softuni.paymentsvc.payments.web.dto.PaymentStatusUpdateRequest;
import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse createPayment(PaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();

        log.info("Creating payment for order {} with amount {} and method {}",
                request.getOrderId(), request.getAmount(), request.getMethod());

        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(existing -> {
                    throw new PaymentAlreadyExistsException(
                            "Payment already exists for order " + request.getOrderId()
                    );
                });

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .createdOn(now)
                .updatedOn(now)
                .build();

        try {
            Payment saved = paymentRepository.save(payment);
            log.info("Payment {} created successfully for order {}", saved.getId(), saved.getOrderId());
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("DataIntegrityViolation when creating payment for order {}. " +
                            "Trying to load existing payment.",
                    request.getOrderId());

            Payment existing = paymentRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> ex);

            log.info("Existing payment {} for order {} returned instead of creating a new one.",
                    existing.getId(), existing.getOrderId());

            return toResponse(existing);
        }
    }

    @Cacheable(value = "payments", key = "#id")
    public PaymentResponse getPayment(UUID id) {
        log.info("Fetching payment {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                log.warn("Payment {} not found", id);
                return new PaymentNotFoundException("Payment with id [%s] not found.".formatted(id));
                });

        return toResponse(payment);
    }

    @Cacheable(value = "payments", key = "'order-' + #orderId")
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        log.info("Fetching payment for order {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment for order {} not found", orderId);
                    return new PaymentNotFoundException("Payment for order [%s] not found.".formatted(orderId));
                });

        return toResponse(payment);
    }

    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse updateStatus(UUID paymentId, PaymentStatusUpdateRequest request) {
        log.info("Updating payment {} status to {}", paymentId, request.getStatus());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment {} not found for status update", paymentId);
                    return new PaymentNotFoundException("Payment with id [%s] not found.".formatted(paymentId));
                });

        payment.setStatus(request.getStatus());
        payment.setUpdatedOn(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        log.info("Payment {} status updated to {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .createdOn(payment.getCreatedOn())
                .updatedOn(payment.getUpdatedOn())
                .build();
    }

    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse processPayment(UUID paymentId) {
        log.info("Processing payment {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment {} not found for processing", paymentId);
                    return new PaymentNotFoundException("Payment with id [%s] not found.".formatted(paymentId));
                });

        if (payment.getStatus() == PaymentStatus.PENDING) {
            log.info("Payment {} is PENDING. Marking as SUCCESSFUL.", paymentId);
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setUpdatedOn(LocalDateTime.now());
            payment = paymentRepository.save(payment);
            log.info("Payment {} processed successfully", paymentId);
        } else {
            log.warn("Payment {} processed but status is {} (only PENDING gets changed).",
                    paymentId, payment.getStatus());
        }
        return toResponse(payment);
    }

}
