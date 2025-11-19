package bg.softuni.paymentsvc.payments.service;

import bg.softuni.paymentsvc.payments.exception.PaymentAlreadyExistsException;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
import bg.softuni.paymentsvc.payments.web.dto.PaymentStatusUpdateRequest;
import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();

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
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            Payment existing = paymentRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> ex);
            return toResponse(existing);
        }
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return toResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment for this order not found"));

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse updateStatus(UUID paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(request.getStatus());
        payment.setUpdatedOn(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
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
    public PaymentResponse processPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setUpdatedOn(LocalDateTime.now());
            payment = paymentRepository.save(payment);
        }

        return toResponse(payment);
    }

}
