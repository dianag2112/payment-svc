package bg.softuni.paymentsvc.payments.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import bg.softuni.paymentsvc.payments.model.PaymentStatus;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusUpdateRequest {

    @NotNull
    private PaymentStatus status;
}
