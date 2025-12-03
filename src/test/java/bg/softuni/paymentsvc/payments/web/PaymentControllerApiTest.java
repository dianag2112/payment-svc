package bg.softuni.paymentsvc.payments.web;

import bg.softuni.paymentsvc.payments.exception.PaymentAlreadyExistsException;
import bg.softuni.paymentsvc.payments.exception.PaymentNotFoundException;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.service.PaymentService;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
import bg.softuni.paymentsvc.payments.web.dto.PaymentStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({PaymentControllerApiTest.TestConfig.class, RestExceptionHandler.class})
class PaymentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestConfig {
        @Bean
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }
    }

    @BeforeEach
    void setup() {
        reset(paymentService);
    }

    @Test
    void createPayment_shouldReturn201AndBody() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("12.50"))
                .method("CARD")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPayment_shouldReturn200AndBody() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("8.40"))
                .method("CARD")
                .status(PaymentStatus.SUCCESSFUL)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentService.getPayment(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.amount").value(8.40));
    }

    @Test
    void getPaymentByOrder_shouldReturn200AndBody() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("15.00"))
                .method("CARD")
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.id").value(paymentId.toString()));
    }

    @Test
    void processPayment_shouldReturn200AndBody() throws Exception {
        UUID paymentId = UUID.randomUUID();

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("19.90"))
                .method("CARD")
                .status(PaymentStatus.SUCCESSFUL)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(paymentId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/{id}/process", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"));
    }

    @Test
    void updateStatus_shouldReturn200AndBody() throws Exception {
        UUID paymentId = UUID.randomUUID();

        PaymentStatusUpdateRequest request = PaymentStatusUpdateRequest.builder()
                .status(PaymentStatus.FAILED)
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("3.50"))
                .method("CARD")
                .status(PaymentStatus.FAILED)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(paymentService.updateStatus(any(UUID.class), any(PaymentStatusUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/{id}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void createPayment_shouldReturn400_onValidationError() throws Exception {
        PaymentRequest invalid = PaymentRequest.builder()
                .orderId(null)
                .amount(new BigDecimal("0.00"))
                .method("")
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"));
    }

    @Test
    void getPayment_shouldReturn404_whenPaymentNotFound() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(paymentService.getPayment(paymentId))
                .thenThrow(new PaymentNotFoundException("Payment with id [" + paymentId + "] not found."));

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void createPayment_shouldReturn409_whenPaymentAlreadyExists() throws Exception {
        UUID orderId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("10.00"))
                .method("CARD")
                .build();

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenThrow(new PaymentAlreadyExistsException("Payment already exists for order " + orderId));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Payment already exists"));
    }
}
