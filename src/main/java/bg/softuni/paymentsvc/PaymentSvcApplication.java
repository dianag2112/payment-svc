package bg.softuni.paymentsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PaymentSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSvcApplication.class, args);
    }

}
