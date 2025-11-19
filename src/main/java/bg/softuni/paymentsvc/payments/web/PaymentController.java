package bg.softuni.paymentsvc.payments.web;

import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
import bg.softuni.paymentsvc.payments.web.dto.PaymentStatusUpdateRequest;
import bg.softuni.paymentsvc.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String health() {
        return "Payments service is up";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable("id") UUID id) {
        return paymentService.getPayment(id);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponse getPaymentByOrder(@PathVariable("orderId") UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @PostMapping("/{id}/process")
    public PaymentResponse processPayment(@PathVariable("id") UUID id) {
        return paymentService.processPayment(id);
    }

    @PostMapping("/{id}")
    public PaymentResponse updateStatus(@PathVariable("id") UUID id,
                                        @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return paymentService.updateStatus(id, request);
    }
}
