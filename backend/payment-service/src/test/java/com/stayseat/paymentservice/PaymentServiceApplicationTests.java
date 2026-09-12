package com.stayseat.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty - just verifies the Spring context wires up.
        // Needs a running Postgres (payment_db) + RabbitMQ locally, same as
        // the other services - see README.md.
    }
}
