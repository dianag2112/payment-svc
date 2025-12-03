package bg.softuni.paymentsvc.payments.service;

import bg.softuni.paymentsvc.payments.exception.PaymentAlreadyExistsException;
import bg.softuni.paymentsvc.payments.exception.PaymentNotFoundException;
import bg.softuni.paymentsvc.payments.model.Payment;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.repository.PaymentRepository;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
import bg.softuni.paymentsvc.payments.web.dto.PaymentStatusUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_shouldCreateNewPayment_whenNoExistingPayment() {
        UUID orderId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("9.90"))
                .method("CARD")
                .build();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        Payment saved = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response.getId());
        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("9.90"), response.getAmount());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("CARD", response.getMethod());

        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrow_whenPaymentAlreadyExists() {
        UUID orderId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("9.90"))
                .method("CARD")
                .build();

        Payment existing = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(existing));

        assertThrows(PaymentAlreadyExistsException.class,
                () -> paymentService.createPayment(request));

        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldReturnExisting_whenSaveCausesDataIntegrityViolation() {
        UUID orderId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("4.20"))
                .method("CARD")
                .build();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(
                        Payment.builder()
                                .id(UUID.randomUUID())
                                .orderId(orderId)
                                .amount(request.getAmount())
                                .method(request.getMethod())
                                .status(PaymentStatus.PENDING)
                                .createdOn(LocalDateTime.now())
                                .updatedOn(LocalDateTime.now())
                                .build()
                ));

        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("4.20"), response.getAmount());
        assertEquals(PaymentStatus.PENDING, response.getStatus());

        verify(paymentRepository, times(2)).findByOrderId(orderId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void updateStatus_shouldChangeStatus() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("5.50"))
                .method("CARD")
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentStatusUpdateRequest request = PaymentStatusUpdateRequest.builder()
                .status(PaymentStatus.SUCCESSFUL)
                .build();

        PaymentResponse response = paymentService.updateStatus(paymentId, request);

        assertEquals(PaymentStatus.SUCCESSFUL, response.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPayment_shouldThrowPaymentNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPayment(id));

        verify(paymentRepository).findById(id);
    }

    @Test
    void getPaymentByOrderId_shouldThrowPaymentNotFound_whenMissing() {
        UUID orderId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(orderId));

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void processPayment_shouldMarkSuccessful_whenPending() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("10.00"))
                .method("CARD")
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(paymentId);

        assertEquals(PaymentStatus.SUCCESSFUL, response.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_shouldNotChangeStatus_whenNotPending() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("10.00"))
                .method("CARD")
                .status(PaymentStatus.SUCCESSFUL)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.processPayment(paymentId);

        assertEquals(PaymentStatus.SUCCESSFUL, response.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
