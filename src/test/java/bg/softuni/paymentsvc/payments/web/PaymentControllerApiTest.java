package bg.softuni.paymentsvc.payments.web;

import bg.softuni.paymentsvc.payments.model.PaymentStatus;
import bg.softuni.paymentsvc.payments.service.PaymentService;
import bg.softuni.paymentsvc.payments.web.dto.PaymentRequest;
import bg.softuni.paymentsvc.payments.web.dto.PaymentResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(PaymentControllerApiTest.TestConfig.class)
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
        Mockito.reset(paymentService);
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
}
