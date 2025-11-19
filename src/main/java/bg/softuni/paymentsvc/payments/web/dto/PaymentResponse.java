package bg.softuni.paymentsvc.payments.web.dto;

import lombok.*;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String method;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
