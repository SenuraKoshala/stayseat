package com.stayseat.paymentservice.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies inbound gateway webhook calls by HMAC-SHA256 signature rather
 * than JWT, per API_CONTRACT.md §4.5 ("none - verify via gateway signature,
 * not JWT"). The signature is computed over the *raw* request body, so the
 * controller must read the body as a string before Jackson binds it to a
 * DTO - see PaymentController.webhook().
 *
 * This mirrors how real gateways do it (e.g. Stripe's Stripe-Signature
 * header): the gateway signs the payload with a secret only it and we know,
 * so we can trust the callback actually came from the gateway and wasn't
 * forged by a third party hitting our public endpoint.
 */
@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;

    public WebhookSignatureVerifier(@Value("${app.payment.webhook-secret}") String secret) {
        this.secret = secret;
    }

    public boolean isValid(String rawBody, String providedSignatureHex) {
        if (providedSignatureHex == null || providedSignatureHex.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            byte[] provided = HexFormat.of().parseHex(providedSignatureHex.trim().toLowerCase());
            return MessageDigest.isEqual(computed, provided);
        } catch (Exception e) {
            // Malformed hex, wrong key spec, etc. - always treat as invalid
            // rather than letting an exception leak signature details.
            return false;
        }
    }
}
