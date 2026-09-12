package com.stayseat.paymentservice.gateway;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookSignatureVerifierTest {

    private static final String SECRET = "test-secret";

    private final WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(SECRET);

    @Test
    void acceptsACorrectlySignedBody() throws Exception {
        String body = "{\"gatewayReference\":\"mock_123\",\"status\":\"SUCCEEDED\"}";
        String signature = sign(body, SECRET);

        assertTrue(verifier.isValid(body, signature));
    }

    @Test
    void rejectsATamperedBody() throws Exception {
        String body = "{\"gatewayReference\":\"mock_123\",\"status\":\"SUCCEEDED\"}";
        String signature = sign(body, SECRET);

        String tamperedBody = "{\"gatewayReference\":\"mock_123\",\"status\":\"FAILED\"}";

        assertFalse(verifier.isValid(tamperedBody, signature));
    }

    @Test
    void rejectsASignatureFromTheWrongSecret() throws Exception {
        String body = "{\"gatewayReference\":\"mock_123\",\"status\":\"SUCCEEDED\"}";
        String signature = sign(body, "wrong-secret");

        assertFalse(verifier.isValid(body, signature));
    }

    @Test
    void rejectsAMissingSignature() {
        assertFalse(verifier.isValid("{}", null));
        assertFalse(verifier.isValid("{}", ""));
    }

    private static String sign(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
